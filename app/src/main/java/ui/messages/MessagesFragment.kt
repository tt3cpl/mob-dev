package ui.messages

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.mobdev.R
import data.repository.ChatRepository
import data.repository.TokenManager
import ui.image.ImageActivity
import ui.login.LoginActivity
import kotlinx.coroutines.launch

class MessagesFragment : Fragment() {
    
    private val viewModel: MessagesViewModel by viewModels {
        val tokenManager = TokenManager(requireContext())
        val repository = ChatRepository(tokenManager, requireContext())
        MessagesViewModelFactory(repository)
    }
    
    private lateinit var adapter: MessageAdapter
    private lateinit var username: String // логин текущего пользователя
    private var channelName: String? = null // название открытого канала
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        channelName = arguments?.getString(ARG_CHANNEL_NAME)
    }
    
    override fun onCreateView( // создаем View из fragment_messages.xml
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupViews()
        observeViewModel()
        
        channelName?.let { viewModel.setChannel(it) }
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter { imageUrl -> // клик по картинке
            startActivity(ImageActivity.newIntent(requireContext(), imageUrl))
        }
        
        val recyclerView = view?.findViewById<RecyclerView>(R.id.messagesRecyclerView) // // находим RecyclerView
        recyclerView?.layoutManager = LinearLayoutManager(requireContext()) // вертикальный список
        recyclerView?.adapter = adapter
        
        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {  // следим за прокруткой
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel.loadMoreMessages()
                }
            }
        })
    }
    
    private fun setupViews() {
        val messageEditText = view?.findViewById<EditText>(R.id.messageEditText)
        val sendButton = view?.findViewById<Button>(R.id.sendButton)
        
        sendButton?.setOnClickListener {
            val text = messageEditText?.text?.toString() ?: "" // берем текст из поля ввода
            if (text.isNotBlank()) {
                viewModel.updateMessageText(text) // сохраняем текст в ViewModel
                viewModel.sendMessage(username) // отправляем сообщение
                messageEditText?.text?.clear() // отчищаем поле ввода
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                val previousSize = adapter.currentList.size
                adapter.submitList(state.messages) // обновляем список сообщений
                
                val recyclerView = view?.findViewById<RecyclerView>(R.id.messagesRecyclerView)
                if (state.messages.size > previousSize && recyclerView != null) { // если появились новые сообщения
                    recyclerView.scrollToPosition(state.messages.size - 1) // скроллим вниз
                }
                
                view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility =
                    if (state.isLoading) View.VISIBLE else View.GONE //показываем загрузку
                
                view?.findViewById<Button>(R.id.sendButton)?.isEnabled = // блокируем кнопку отправки
                    !state.isSending
            }
        }
        
        lifecycleScope.launch {
            val tokenManager = TokenManager(requireContext())
            tokenManager.credentials.collect { (user, _) ->
                username = user ?: "" // сохраняем имя пользователя
            }
        }
        
        lifecycleScope.launch {
            val tokenManager = TokenManager(requireContext())
            tokenManager.isLoggedIn.collect { isLoggedIn -> // если токен удалили открываем окно логина
                if (!isLoggedIn) {
                    requireActivity().startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                }
            }
        }
    }
    
    companion object {
        private const val ARG_CHANNEL_NAME = "channel_name"

    }
}
