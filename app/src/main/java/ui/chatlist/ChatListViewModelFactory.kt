package ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import data.repository.ChatRepository

// класс который создает ChatListViewModel
class ChatListViewModelFactory(
    private val repository: ChatRepository // данные: API + DB + сеть
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T { // метод, который Android вызывает для создания ViewModel
        if (modelClass.isAssignableFrom(ChatListViewModel::class.java)) { // проверяем нужен ли именно ChatListViewModel
            return ChatListViewModel(repository) as T // создаём ViewModel и передаём туда repository
        }
        throw IllegalArgumentException("Неизвестный ViewModel")
    }
}
