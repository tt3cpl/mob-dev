package data.model


// тоже самое создает тело только для сообщения
data class SendMessageRequest(
    val from: String,
    val to: String? = null,
    val data: MessageData
)
