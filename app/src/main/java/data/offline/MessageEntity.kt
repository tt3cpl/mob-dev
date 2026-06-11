package data.offline

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

// Таблица сообщений — все поля сообщения
@Entity(
    tableName = "messages",
    indices = [Index(value = ["channelName", "id"], unique = true)]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val channelName: String,
    val from: String,
    val text: String?, // потому что может быть картинка
    val imageThumb: String?, // путь к превью картинки
    val imageFull: String?, // путь к полной картинки
    val timestamp: Long
)
