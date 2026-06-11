package data.offline

import androidx.room.Entity
import androidx.room.PrimaryKey

// таблица каналов — просто одна колонка имя канала
@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val name: String // первичный ключ ну эт очевидно просто чтобы не заабыть
)
