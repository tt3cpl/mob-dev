package data.model

// создает тело для post запроса при логине
data class LoginRequest(
    val name: String,
    val pwd: String
)
