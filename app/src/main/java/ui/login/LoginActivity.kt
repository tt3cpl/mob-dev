package ui.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import app.mobdev.R
import data.repository.ChatRepository
import data.repository.TokenManager
import ui.chatlist.ChatListActivity
import kotlinx.coroutines.launch


// экуран логина
class LoginActivity : AppCompatActivity() {

    // создаем view
    private val viewModel: LoginViewModel by viewModels {
        val tokenManager = TokenManager(this) // работа с datastore
        val repository = ChatRepository(tokenManager, this)
        LoginViewModelFactory(repository, tokenManager) // фабрика view
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // подключаем XML разметку экрана
        setupViews() // настройка кнопок и полей
        observeViewModel()
    }
    
    private fun setupViews() {
        // находям пол ввода логина и пароля
        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton) // кнопка входа
        // подключаем обработчик кнопки
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            viewModel.login(username, password)
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch { // запускаем корутину, привязанную к жизненному циклу Activity
            viewModel.uiState.collect { state ->
                if (state.isLoggedIn) { // если логин успешен переходим на следующий экран
                    startActivity(ChatListActivity.newIntent(this@LoginActivity))
                    finish() // заркваем loginActivity
                }
                // если есть ошибка показываем ее
                state.errorMessage?.let { error ->
                    showErrorDialog(error)
                    viewModel.clearError() // отчищаем чтобы не показывалась снова
                }
            }
        }
    }
    
    private fun showErrorDialog(message: String) { // создаем всплывающее окно
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.error)) // заголовок
            .setMessage(message) // текст ошибки
            .setPositiveButton(getString(R.string.ok), null) // кнопка ОК
            .show()
    }
    
    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    @Deprecated("Deprecated in Java")

    // закрывает ВСЕ экраны приложения
    override fun onBackPressed() {
        finishAffinity()
    }
}
