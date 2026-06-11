package data.api

import android.annotation.SuppressLint
import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


// retrofit
@SuppressLint("StaticFieldLeak")
object RetrofitClient { // синглтон
    
    private const val BASE_URL = "https://faerytea.name/"
    
    private var context: Context? = null
    
    fun init(context: Context) {
        this.context = context.applicationContext
    }

    // логируем все в logcat
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // для http запросов
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val authInterceptor = context?.let { AuthInterceptor(it) }
            authInterceptor?.intercept(chain) ?: chain.proceed(chain.request())
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ChatApiService = retrofit.create(ChatApiService::class.java)
}
