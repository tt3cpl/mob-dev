package data.offline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// // инетрфейс для работы с таблицей сообщений. Room генерирует SQL-запросы на основе аннотаций

@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE channelName = :channelName ORDER BY id DESC LIMIT :limit")
    fun getMessagesByChannel(channelName: String, limit: Int = 20): Flow<List<MessageEntity>> //flow чтобы автоматический получать данные
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    
    @Query("DELETE FROM messages WHERE channelName = :channelName")
    suspend fun clearMessagesByChannel(channelName: String)
    
    @Query("DELETE FROM messages")
    suspend fun clearAll()
}
