package ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.repository.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// состояние экрана списка чатов
data class ChatListUiState(
    val isLoading: Boolean = false, // идет ли загрузка каналов
    val channels: List<String> = emptyList(), // список каналов
    val errorMessage: String? = null, // ошибка
    val shouldLogout: Boolean = false, // флаг для выхода из аккаунта
    val isOffline: Boolean = false // проверка сети
)


// ViewModel для экрана списка каналов
class ChatListViewModel(
    private val repository: ChatRepository // слой с апи
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState()) // внутреннее изменяемое состояние
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow() // внешнее
    
    private var wasOffline = false // флаг, чтобы понимать, был ли оффлайн раньше
    
    init { // сразу при создании
        loadChannels() // загружаем каналы с сервера
        observeCachedChannels() // слушаем кеш
        observeNetworkStatus() // слушаем связь
    }
    
    fun loadChannels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null) // включаем загрузку и очищаем ошибку
            
            val result = repository.getChannels() // запрос к репозиторию
            result.fold(
                onSuccess = { channels -> // если успех то обновляем UI
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        channels = channels,
                        isOffline = false
                    )
                },
                onFailure = { _ -> // если нет то выводим ошибку
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Ошибка загрузки каналов",
                        isOffline = !repository.isOnline() // делаем проверку интернета
                    )
                }
            )
        }
    }
    
    private fun observeCachedChannels() {
        viewModelScope.launch {
            repository.getCachedChannels().collect { channels -> // подписка на локальную базу
                if (channels.isNotEmpty() && _uiState.value.channels.isEmpty()) { // если пришли кешированные данные и экран пустой
                    _uiState.value = _uiState.value.copy(channels = channels) // показываем кеш
                }
            }
        }
    }
    
    private fun observeNetworkStatus() {
        viewModelScope.launch {
            while (true) {
                delay(2000) // каждые 2 секунды проверяем интернет
                val isOnline = repository.isOnline()
                val currentIsOffline = _uiState.value.isOffline
                
                if (isOnline && currentIsOffline) { // если интернет вернулся то перезагружаем данные
                    loadChannels()
                }
                
                _uiState.value = _uiState.value.copy(isOffline = !isOnline) // обновляем статус интернета
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            repository.logout() // очищаем токен + кеш + данные
            _uiState.value = _uiState.value.copy(shouldLogout = true)
        }
    }
    
    fun clearLogoutFlag() { // сброс флага выхода
        _uiState.value = _uiState.value.copy(shouldLogout = false)
    }
}
