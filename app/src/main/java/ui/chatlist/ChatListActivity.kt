package ui.chatlist

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.mobdev.R
import data.repository.ChatRepository
import data.repository.TokenManager
import ui.login.LoginActivity
import ui.messages.MessagesActivity
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class ChatListActivity : AppCompatActivity() {
    
    private val viewModel: ChatListViewModel by viewModels {
        val tokenManager = TokenManager(this) // работа с токеном
        val repository = ChatRepository(tokenManager, this) // работа с апи и со стором
        ChatListViewModelFactory(repository) // создаем view
    }
    
    private lateinit var adapter: ChannelAdapter // адаптер
    private var selectedChannel: String? = null // выбранный канал
    private lateinit var tokenManager: TokenManager // хранение токена
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list) // подключаем layout
        supportActionBar?.title = "Чаты" // ставим заголовок
        tokenManager = TokenManager(this) // создаем TokenManager (для проверки логина)

        // настройка ui
        setupRecyclerView()
        setupLogoutButton()
        observeViewModel()
        
        selectedChannel = savedInstanceState?.getString(SELECTED_CHANNEL_KEY) // восстановление выбранного канала после пересоздания экрана
    }

    // вызывается при смене конфигурации(при повороте)
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    // сохраняем состояние экрана
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedChannel?.let { outState.putString(SELECTED_CHANNEL_KEY, it) }
    }
    
    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerView() {
        adapter = ChannelAdapter { channel -> // создаем адаптер
            selectedChannel = channel // клик по каналу
            adapter.selectedChannel = channel
            adapter.notifyDataSetChanged() // обновляем список
            openMessagesScreen(channel) // открываем канал сообщений
        }

        // находим RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.channelsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this) // задаем layout (вертикальный список)
        recyclerView.adapter = adapter // подключаем адаптер
    }
    
    private fun setupLogoutButton() {
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener { // вызываем logout во ViewModel
            viewModel.logout()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch { // подписка на состояние
            viewModel.uiState.collect { state -> // обновляем список каналов
                adapter.submitList(state.channels)
                
                val progressBar = findViewById<View>(R.id.progressBar) // показываем/скрываем прогресс бар
                progressBar?.visibility = 
                    if (state.isLoading) View.VISIBLE else View.GONE
                
                state.errorMessage?.let { error ->
                    showErrorDialog(error) // если ошибка показываем
                }
                
                if (state.shouldLogout) {
                    viewModel.clearLogoutFlag() // если нужно выйти из аккаунта
                    startActivity(Intent(this@ChatListActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }
        
        lifecycleScope.launch { // отдельная подписка на состояние логина
            tokenManager.isLoggedIn.collect { isLoggedIn ->
                if (!isLoggedIn) {
                    startActivity(Intent(this@ChatListActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }
    
    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter.selectedChannel = selectedChannel // восстанавливаем выбранный канал
        adapter.notifyDataSetChanged()
    }
    
    private fun openMessagesScreen(channel: String) { // переход на экран сообщений
        startActivity(MessagesActivity.newIntent(this, channel))
    }
    
    @SuppressLint("GestureBackNavigation")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { // закрывает всё приложение
        super.onBackPressed()
        finishAffinity()
    }
    
    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.error))
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }
    
    companion object {
        private const val SELECTED_CHANNEL_KEY = "selected_channel"
        
        fun newIntent(context: Context): Intent {
            return Intent(context, ChatListActivity::class.java)
        }
    }
}
