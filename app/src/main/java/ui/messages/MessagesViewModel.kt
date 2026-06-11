package ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.model.Message
import data.repository.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MessagesUiState(
    val isLoading: Boolean = false, // идет ли загрузка сообщений
    val messages: List<Message> = emptyList(), // список сообщений
    val errorMessage: String? = null, // для ошибки
    val messageText: String = "", // текст сообщения
    val isSending: Boolean = false, // идет ли отправка
    val channelName: String = "", // название канала
    val isOffline: Boolean = false // есть ли сеть
)

class MessagesViewModel(
    private val repository: ChatRepository // API + DB + сеть
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MessagesUiState()) // внутреннее изменяемое состояние
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow() // внешнее
    
    private var currentLastId: Long = 0 // id последнего сообщения
    private var currentChannelName: String = "" // текущий канал
    
    fun setChannel(channelName: String) {
        currentChannelName = channelName
        _uiState.value = _uiState.value.copy(channelName = channelName, messages = emptyList()) // сбрасываем UI под новый канал
        currentLastId = 0
        loadMessages(channelName) // загружаем сообщения
        observeCachedMessages(channelName) // слушаем кеш
        observeNetworkStatus() // следим за интернетом
    }

    fun loadMessages(channelName: String, loadMore: Boolean = false) {
        viewModelScope.launch {
            if (!loadMore) {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null) // если это НЕ подгрузка то показываем loading
            }
            
            val result = repository.getMessages( // запрос к серверу
                channelName = channelName,
                limit = 20,
                lastKnownId = currentLastId,
                reverse = false
            )
            
            result.fold(
                onSuccess = { messages ->
                    val newMessages = if (loadMore) { // если loadMore то добавляем к старым
                        _uiState.value.messages + messages
                    } else {
                        messages
                    }
                    
                    if (messages.isNotEmpty()) { // обновляем lastId для следующей страницы
                        currentLastId = messages.last().id.toLongOrNull() ?: currentLastId
                    }
                    
                    _uiState.value = _uiState.value.copy( // обновляем UI
                        isLoading = false,
                        messages = newMessages,
                        isOffline = false
                    )
                },
                onFailure = {
                    _ ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Ошибка загрузки сообщений",
                        isOffline = !repository.isOnline()
                    )
                }
            )
        }
    }
    
    private fun observeNetworkStatus() {
        viewModelScope.launch {
            while (true) {
                delay(2000) // каждые 2 секунды проверяем интернет
                val isOnline = repository.isOnline()
                val currentIsOffline = _uiState.value.isOffline
                // если вернулся то продолжаем загрузку
                if (isOnline && currentIsOffline && currentChannelName.isNotEmpty()) {
                    loadMessages(currentChannelName)
                }
                // обновляем статус
                _uiState.value = _uiState.value.copy(isOffline = !isOnline)
            }
        }
    }
    
    fun updateMessageText(text: String) {
        _uiState.value = _uiState.value.copy(messageText = text)
    }
    
    fun sendMessage(username: String) {
        val text = _uiState.value.messageText
        if (text.isBlank()) return // если пусто то ничего не отправляем
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            
            val result = repository.sendMessage( // отправка через репозиторий
                username = username,
                text = text,
                channelName = _uiState.value.channelName
            )
            
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy( // очищаем поле ввода
                        isSending = false,
                        messageText = ""
                    )
                    loadMessages(_uiState.value.channelName) // перезагружаем сообщения
                },
                onFailure = { _ ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        errorMessage = "Ошибка отправки"
                    )
                }
            )
        }
    }
    
    fun loadMoreMessages() {
        if (!_uiState.value.isLoading) { // если сейчас не загружается то грузим следующую страницу
            loadMessages(_uiState.value.channelName, loadMore = true)
        }
    }
    
    private fun observeCachedMessages(channelName: String) {
        viewModelScope.launch { // показываем кеш только если экран пустой
            repository.getCachedMessages(channelName).collect { messages ->
                if (messages.isNotEmpty() && _uiState.value.messages.isEmpty()) {
                    _uiState.value = _uiState.value.copy(messages = messages)
                }
            }
        }
    }
}
