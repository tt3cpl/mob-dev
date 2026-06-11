package ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import data.repository.ChatRepository


// фабрика для создания MasageModal

class MessagesViewModelFactory(
    private val repository: ChatRepository // зависимость работа с апи
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessagesViewModel::class.java)) { // проверяем, что запрашивается MessagesViewModel
            return MessagesViewModel(repository) as T // создаем ViewModel
        }
        throw IllegalArgumentException("Неизвестный ViewModel")
    }
}
