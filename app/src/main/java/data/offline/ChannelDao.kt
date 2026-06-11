package data.offline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// инетрфейс для работы с таблицей каналов. Room генерирует SQL-запросы на основе аннотаций

@Dao
interface ChannelDao {
    
    @Query("SELECT * FROM channels")
    fun getAllChannels(): Flow<List<ChannelEntity>> //flow чтобы автоматический получать данные
    
    @Insert(onConflict = OnConflictStrategy.REPLACE) // дубликаты обновляем
    suspend fun insertChannel(channel: ChannelEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE) // дубликаты обновляем
    suspend fun insertChannels(channels: List<ChannelEntity>)
    
    @Query("DELETE FROM channels")
    suspend fun clearAll()
}
