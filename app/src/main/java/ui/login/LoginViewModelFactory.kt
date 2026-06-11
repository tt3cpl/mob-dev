package ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import data.repository.ChatRepository
import data.repository.TokenManager

// фабрика для создания LoginViewModel
class LoginViewModelFactory( // класс, который создает ViewModel
    private val repository: ChatRepository, // зависимость работа с апи
    private val tokenManager: TokenManager // зависимость работа с дата стором
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T { // метод создание view
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) { // проверяем нужен ли именно LoginViewModel
            return LoginViewModel(repository, tokenManager) as T // создаем LoginViewModel и передаем зависимости
        }
        throw IllegalArgumentException("Неизвестный ViewModel")// если запросили другую ViewModel — ошибка
    }
}
