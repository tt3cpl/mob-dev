package ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import app.mobdev.R
import data.model.Message

// Адаптер для списка сообщений
class MessageAdapter(
    private val onImageClick: (String) -> Unit // вызывается при клике на картинку
) : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder { // cоздание нового ViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false) // загружаем item_message.xml
        return MessageViewHolder(view, onImageClick)
    }
    
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) { // заполнение ViewHolder данными
        holder.bind(getItem(position))
    }
    
    class MessageViewHolder( // один элемент списка сообщений
        itemView: View,
        private val onImageClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val fromTextView: TextView = itemView.findViewById(R.id.fromTextView) // автор
        private val textTextView: TextView = itemView.findViewById(R.id.textTextView) // текст
        private val imageView: ImageView = itemView.findViewById(R.id.imageView) // картинка сообщения
        
        fun bind(message: Message) {
            fromTextView.text = message.from // показываем автора
            
            message.data.text?.let { textData ->
                textTextView.text = textData.text // если текст есть
                textTextView.visibility = View.VISIBLE
            } ?: run { // если неста нет
                textTextView.visibility = View.GONE
            }
            
            message.data.image?.let { imageData ->
                if (imageData.link != null) { // если ссыока на картинку существует
                    val thumbUrl = "https://faerytea.name/thumb/${imageData.link}"
                    imageView.load(thumbUrl)// показывает ее в ImageView
                    imageView.visibility = View.VISIBLE
                    imageView.setOnClickListener { // клик по картинке
                        val fullUrl = "https://faerytea.name/img/${imageData.link}" // URL оригинальной картинки
                        onImageClick(fullUrl)
                    }
                } else {
                    imageView.visibility = View.GONE
                }
            } ?: run {
                imageView.visibility = View.GONE // если картинки нет
            }
        }
    }
    
    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}
