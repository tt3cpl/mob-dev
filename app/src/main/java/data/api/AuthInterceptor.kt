package data.api

import android.content.Context
import data.repository.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    context: Context
) : Interceptor {
    
    private val tokenManager = TokenManager(context)
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request() // исходный запрос
        
        val token = runBlocking {
            tokenManager.token.first() //берем токен из DataStore
        }
        
        val requestBuilder = originalRequest.newBuilder()
        
        if (token != null) {
            requestBuilder.header("X-Auth-Token", token) // добавляем заголовок
        }
        
        val request = requestBuilder.build() // выполняем запрос
        val response = chain.proceed(request)
        
        if (response.code == 401) { // если токен протух
            runBlocking {
                tokenManager.clearAll() // мы его разлогиниваем
            }
        }
        
        return response
    }
}
