package data.offline

import data.model.ImageData
import data.model.Message
import data.model.MessageData
import data.model.TextData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineCache(private val database: AppDatabase) {
    // возвращает Flow со списком каналов
    fun getChannels(): Flow<List<String>> {
        return database.channelDao().getAllChannels().map { entities ->
            entities.map { it.name }
        }
    }
    
    suspend fun saveChannels(channels: List<String>) {
        database.channelDao().insertChannels(channels.map { ChannelEntity(it) })
    }

    // конвентирует massageEntity в Massege
    fun getMessages(channelName: String): Flow<List<Message>> {
        return database.messageDao().getMessagesByChannel(channelName).map { entities ->
            entities.map { entity ->
                Message(
                    id = entity.id,
                    from = entity.from,
                    to = channelName,
                    data = if (entity.text != null) {
                        MessageData(text = TextData(entity.text), image = null)
                    } else if (entity.imageThumb != null) {
                        MessageData(text = null, image = ImageData(entity.imageThumb))
                    } else {
                        MessageData(text = TextData(""), image = null)
                    },
                    time = entity.timestamp
                )
            }
        }
    }
    
    suspend fun saveMessages(messages: List<Message>, channelName: String) {
        val entities = messages.map { message ->
            val text = message.data.text?.text
            val imageLink = message.data.image?.link
            MessageEntity(
                id = message.id,
                channelName = channelName,
                from = message.from,
                text = text,
                imageThumb = imageLink,
                imageFull = imageLink,
                timestamp = message.time ?: System.currentTimeMillis()
            )
        }
        database.messageDao().insertMessages(entities)
    }

    suspend fun clearAll() {
        database.channelDao().clearAll()
        database.messageDao().clearAll()
    }
}
