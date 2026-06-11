package data.api

import data.model.LoginRequest
import data.model.Message
import data.model.SendMessageRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*



interface ChatApiService {
    
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<ResponseBody>
    
    @POST("logout")
    suspend fun logout(): Response<Unit>
    
    @GET("channels")
    suspend fun getChannels(): Response<List<String>>
    
    @GET("channel/{channelName}")
    suspend fun getMessages(
        @Path("channelName") channelName: String,
        @Query("limit") limit: Int = 20,
        @Query("lastKnownId") lastKnownId: Long = 0,
        @Query("reverse") reverse: Boolean = false
    ): Response<List<Message>>
    
    @POST("messages")
    suspend fun sendMessage(
        @Body request: SendMessageRequest,
        @Header("X-Auth-Token") token: String
    ): Response<ResponseBody>

}
