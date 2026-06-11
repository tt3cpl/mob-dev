package ui.messages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.mobdev.R
import data.repository.ChatRepository
import data.repository.TokenManager
import ui.image.ImageActivity
import ui.login.LoginActivity
import kotlinx.coroutines.launch


// экран сообщений конкретного канала
class MessagesActivity : AppCompatActivity() {
    // создание ViewModel через Factory
    private val viewModel: MessagesViewModel by viewModels {
        val tokenManager = TokenManager(this)
        val repository = ChatRepository(tokenManager, this)
        MessagesViewModelFactory(repository)
    }
    
    private lateinit var adapter: MessageAdapter // адаптер списка сообщений
    private lateinit var username: String // имя пользователя

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages) // подключаем XML разметку
        
        val channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME) ?: return // получаем имя канала из Intent
        
        setupRecyclerView() // настройка списка
        setupViews() // найтрока кнопок
        observeViewModel() // подписка
        
        viewModel.setChannel(channelName)
    }
    
    private fun setupRecyclerView() {
        adapter = MessageAdapter { imageUrl -> // клик по изображению в сообщении
            startActivity(ImageActivity.newIntent(this, imageUrl))
        }
        
        val recyclerView = findViewById<RecyclerView>(R.id.messagesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this) // вертикальный список
        recyclerView.adapter = adapter
        
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() { // слушатель прокрутки списка
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) { //если дошли до конца списка
                    viewModel.loadMoreMessages() // згружаем следующую порцию сообщений
                }
            }
        })
    }
    
    private fun setupViews() {
        val backButton = findViewById<Button>(R.id.backButton) // Кнопка назад
        backButton.setOnClickListener {
            finish() // закрываем экран
        }
        
        val messageEditText = findViewById<EditText>(R.id.messageEditText)
        val sendButton = findViewById<Button>(R.id.sendButton)
        
        sendButton.setOnClickListener { // Кнопка отправки сообщения
            val text = messageEditText.text.toString()
            if (text.isNotBlank()) {
                viewModel.updateMessageText(text) // сохраняем текст во ViewModel
                viewModel.sendMessage(username) // отправляем сообщение
                messageEditText.text.clear() // очищаем поле ввода
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch { // Подписка на состояние экрана
            viewModel.uiState.collect { state ->
                val previousSize = adapter.currentList.size // размер списка ДО обновления
                adapter.submitList(state.messages) // Обновляем список сообщений
                
                val recyclerView = findViewById<RecyclerView>(R.id.messagesRecyclerView)
                if (state.messages.size > previousSize) { //если появились новые сообщения
                    recyclerView.scrollToPosition(state.messages.size - 1) // Скроллим вниз
                }
                
                findViewById<ProgressBar>(R.id.progressBar).visibility = // показываем/скрываем ProgressBar
                    if (state.isLoading) View.VISIBLE else View.GONE
                
                findViewById<Button>(R.id.sendButton).isEnabled = // блокируем кнопку отправки
                    !state.isSending && !state.isOffline
            }
        }
        
        lifecycleScope.launch {
            val tokenManager = TokenManager(this@MessagesActivity) // получаем имя пользователя из DataStore
            tokenManager.credentials.collect { (user, _) ->
                username = user ?: ""
            }
        }
        
        lifecycleScope.launch { // следим за авторизацией
            val tokenManager = TokenManager(this@MessagesActivity)
            tokenManager.isLoggedIn.collect { isLoggedIn ->
                if (!isLoggedIn) {  // если токен удален возвращаем на экран логина
                    startActivity(Intent(this@MessagesActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA_CHANNEL_NAME = "channel_name" // ключ для Intent

        fun newIntent(context: Context, channelName: String): Intent { // связка Intent
            return Intent(context, MessagesActivity::class.java).apply {
                putExtra(EXTRA_CHANNEL_NAME, channelName)
            }
        }
    }
}
