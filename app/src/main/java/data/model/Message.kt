package data.model

// описание структуры сообщения

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("from")
    val from: String,
    
    @SerializedName("to")
    val to: String?,
    
    @SerializedName("data")
    val data: MessageData,
    
    @SerializedName("time")
    val time: Long?
)

data class MessageData(
    @SerializedName("Text")
    val text: TextData?,
    
    @SerializedName("Image")
    val image: ImageData?
)

data class TextData(
    @SerializedName("text")
    val text: String
)

data class ImageData(
    @SerializedName("link")
    val link: String?
)
