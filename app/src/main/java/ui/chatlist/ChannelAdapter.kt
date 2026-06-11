package ui.chatlist

import android.R as AndroidR
import app.mobdev.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ChannelAdapter( // Adapter для списка каналов
    private val onChannelClick: (String) -> Unit // callback при клике на канал
) : ListAdapter<String, ChannelAdapter.ChannelViewHolder>(ChannelDiffCallback()) {
    
    var selectedChannel: String? = null // выбранный канал для подстветки
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder { // создаем ViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel, parent, false) // наполняем layout item_channel.xml
        return ChannelViewHolder(view, onChannelClick) // возвращаем ViewHolder
    }
    
    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) { // привязываем данные к ViewHolder
        holder.bind(getItem(position), selectedChannel)
    }
    
    class ChannelViewHolder(
        itemView: View,
        private val onChannelClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val textView: TextView = itemView.findViewById(R.id.channelTextView) // TextView внутри item_channel.xml
        
        fun bind(channel: String, selectedChannel: String?) { // привязка данных к одному элементу списка
            textView.text = channel // показываем название канала
            
            if (channel == selectedChannel) { // если канал выбран мы его подсвечиваем
                itemView.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, AndroidR.color.holo_blue_light) // голубым
                )
            } else {
                itemView.setBackgroundColor( // если нет то прозначный фон
                    ContextCompat.getColor(itemView.context, AndroidR.color.transparent)
                )
            }
            
            itemView.setOnClickListener { onChannelClick(channel) } // обработка клика по элементу
        }
    }
    
    class ChannelDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
        
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
