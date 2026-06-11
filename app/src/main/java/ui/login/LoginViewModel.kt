package ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.repository.ChatRepository
import data.repository.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState( // модель состояния
    val isLoading: Boolean = false, // идет ли загрузка (крутящаеся колесо)
    val isLoggedIn: Boolean = false, // залогиненн ли пользователь
    val errorMessage: String? = null, // сообщение об ошибки
    val username: String = "", // введенный логин
    val password: String = "" // введенный апроль
)

class LoginViewModel( // посредник между ui и ?беком?
    private val repository: ChatRepository, // работа с сервером и данными
    private val tokenManager: TokenManager // доступ к сохраненным данным
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState()) // изменения состояния только внутри view
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()  // публичное состояние
    
    init {
        loadSavedCredentials() // вызывается при создании viewModel
    }
    
    private fun loadSavedCredentials() { //  загрузка сохраненных логина и пароля из DataStore
        viewModelScope.launch {
            tokenManager.credentials.collect { (username, password) ->
                if (username != null && password != null) {
                    _uiState.value = _uiState.value.copy(
                        username = username, // подставляем логин
                        password = password // подставляем пароль
                    )
                }
            }
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) { // проверка на пустые поля
            _uiState.value = _uiState.value.copy(
                errorMessage = "Заполните все поля"
            )
            return
        }
        
        viewModelScope.launch { // удаляем ошибку
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = repository.login(username, password) // обработка результата
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, // вкулючаем загрузку
                        isLoggedIn = true, // ставим флаг входа
                        errorMessage = null // убираем ошибки
                    )
                },
                onFailure = { _ ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, // включаем загрузку
                        isLoggedIn = false, // не вошли
                        errorMessage = "Неверный логин или пароль" // выводим ошибку
                    )
                }
            )
        }
    }

    // очищаем ошибки если происходит какоето действие (например печатаем)
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
