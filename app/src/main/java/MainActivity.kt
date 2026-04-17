package io.github.mobdev

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class Contact(val name: String?, val phoneNumber: String?, val email: String?)

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var contactAdapter: ContactAdapter
    private var contactsList = mutableListOf<Contact>()
    private var isContactsLoaded = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            loadContacts()
        } else {
            showNoPermissionMessage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView)
        
        contactAdapter = ContactAdapter(emptyList()) { contact ->
            showContactDetails(contact)
        }
        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
        contactsRecyclerView.adapter = contactAdapter

        if (savedInstanceState != null) {
            val savedContacts = savedInstanceState.getSerializable("contacts") as? ArrayList<Contact>
            if (savedContacts != null) {
                contactsList = savedContacts.toMutableList()
                contactAdapter.updateContacts(contactsList)
                statusText.text = getString(R.string.contacts_found, contactsList.size)
                isContactsLoaded = true
            } else {
                checkPermissionAndLoad()
            }
        } else {
            checkPermissionAndLoad()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("contacts", ArrayList(contactsList))
    }

    private fun checkPermissionAndLoad() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                loadContacts()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun loadContacts() {
        if (isContactsLoaded && contactsList.isNotEmpty()) {
            return
        }
        
        statusText.text = getString(R.string.loading_contacts)
        contactsList.clear()
        contactAdapter.updateContacts(emptyList())

        try {
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val contactIdIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                
                while (cursor.moveToNext()) {
                    val name = if (nameIndex >= 0) cursor.getString(nameIndex) else null
                    val phoneNumber = if (phoneIndex >= 0) cursor.getString(phoneIndex) else null
                    val contactId = if (contactIdIndex >= 0) cursor.getString(contactIdIndex) else null
                    
                    if (name != null && contactId != null) {
                        val email = getEmailForContact(contactId)
                        contactsList.add(Contact(name, phoneNumber, email))
                    }
                }
            }

            if (contactsList.isEmpty()) {
                statusText.text = getString(R.string.contacts_not_found)
            } else {
                contactsList.sortBy { it.name?.lowercase() ?: "" }
                
                statusText.text = getString(R.string.contacts_found, contactsList.size)
                contactAdapter.updateContacts(contactsList)
                isContactsLoaded = true
            }
        } catch (e: Exception) {
            Log.e("MainActivity", getString(R.string.error_loading_contacts), e)
            statusText.text = getString(R.string.error_loading_contacts)
        }
    }


    private fun showContactDetails(contact: Contact) {
        val detailText = getString(R.string.contact_details, 
            contact.name ?: getString(R.string.not_specified),
            contact.phoneNumber ?: getString(R.string.not_specified),
            contact.email ?: getString(R.string.not_specified)
        )

        statusText.text = detailText
    }

    private fun getEmailForContact(contactId: String): String? {
        return try {
            contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                arrayOf(contactId),
                null
            )?.use { cursor ->
                val emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                if (cursor.moveToFirst() && emailIndex >= 0) {
                    cursor.getString(emailIndex)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", getString(R.string.error_getting_email), e)
            null
        }
    }

    private fun showNoPermissionMessage() {
        statusText.text = getString(R.string.no_permission)
        contactAdapter.updateContacts(emptyList())
    }
}
