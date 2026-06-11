package data.offline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// точка доступа ко всей локальной базе данных

@Database(
    entities = [ChannelEntity::class, MessageEntity::class], // сами таблицы
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun channelDao(): ChannelDao // доступ к таблице каналов
    abstract fun messageDao(): MessageDao //доступ к таблице сообщений
    
    companion object {
        @Volatile // гарантирует видимость переменной INSTANCE
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mobdev_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
