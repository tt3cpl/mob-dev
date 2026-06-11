package data.repository

import android.content.Context
import data.api.ChatApiService
import data.api.RetrofitClient
import data.model.LoginRequest
import data.model.Message
import data.model.MessageData
import data.model.SendMessageRequest
import data.model.TextData
import data.offline.AppDatabase
import data.offline.NetworkMonitor
import data.offline.OfflineCache
import kotlinx.coroutines.flow.first

// единственная точка доступа к данным для ViewModels

class ChatRepository(
    private val tokenManager: TokenManager, // отвечает за хранение токена/логина
    context: Context // нужен для базы и проверки сети
) {
    
    private val apiService: ChatApiService = RetrofitClient.apiService
    private val database = AppDatabase.getDatabase(context)
    private val offlineCache = OfflineCache(database)
    private val networkMonitor = NetworkMonitor(context)

    // сам логин

    suspend fun login(username: String, password: String): Result<String> {
        return try {
            val response = apiService.login(LoginRequest(username, password)) // отправка запроса на сервер
            if (response.isSuccessful && response.body() != null) { // проверка успешного ответа и тела
                val token = response.body()!!.string() // получаем токен
                tokenManager.saveToken(token) // сохраняем токен
                tokenManager.saveCredentials(username, password) // сохраняем данные для логина
                Result.success(token) // возвращаем успех
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // выход
    suspend fun logout(): Result<Unit> {
        return try {
            // запрос на сервер
            apiService.logout()
            tokenManager.clearAll()
            offlineCache.clearAll()
            Result.success(Unit)
        } catch (_: Exception) {
            // даже если севрер умер то все равно отчищаем (ну или если интернета нет)
            tokenManager.clearAll()
            offlineCache.clearAll()
            Result.success(Unit)
        }
    }

    // получаем каналы
    suspend fun getChannels(): Result<List<String>> {
        return try {
            val response = apiService.getChannels()
            if (response.isSuccessful && response.body() != null) {
                val channels = response.body()!!
                // сохраняем каналы в кеш
                offlineCache.saveChannels(channels)
                Result.success(channels)
            } else {
                Result.failure(Exception("Ошибка в получении каналов "))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // получить каналы из кеша (без интернета)
    fun getCachedChannels() = offlineCache.getChannels()

    // получить сообщения
    suspend fun getMessages(
        channelName: String,
        limit: Int = 20,
        lastKnownId: Long = 0,
        reverse: Boolean = false
    ): Result<List<Message>> {
        return try {
            val response = apiService.getMessages(channelName, limit, lastKnownId, reverse)
            if (response.isSuccessful && response.body() != null) {
                val messages = response.body()!!
                // тоже сохраняем в кеш сразу
                offlineCache.saveMessages(messages, channelName)
                Result.success(messages)
            } else {
                Result.failure(Exception("Ошибка в получении сообщений"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // получаить сообщения из кеша
    fun getCachedMessages(channelName: String) = offlineCache.getMessages(channelName)

    // отправить сообщение
    suspend fun sendMessage(
        username: String,
        text: String,
        channelName: String? = null
    ): Result<String> {
        if (!networkMonitor.isCurrentlyOnline()) {
            return Result.failure(Exception("Нет подключения к интернету"))
        }
        
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("Нет токена")) // получаем токен
            val request = SendMessageRequest( // формируем тело запроса
                from = username,
                to = channelName,
                data = MessageData(text = TextData(text), image = null)
            )
            // отправка сообщения на сервер
            val response = apiService.sendMessage(request, token)
            if (response.isSuccessful) { // возвращаем ответ сервера
                Result.success(response.body()?.string() ?: "")
            } else {
                Result.failure(Exception("Ошибка в отправке сообщения"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // проверка онлайн/оффлайн состояния
    fun isOnline(): Boolean = networkMonitor.isCurrentlyOnline()
}
