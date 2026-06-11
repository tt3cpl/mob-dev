package data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


// создает datastore с именем auth. оно асинхронное, потокобезопасное(так сказал гугл)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth") // привязываем датастор к Context

class TokenManager(private val context: Context) {
    
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token") // токен
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val PASSWORD_KEY = stringPreferencesKey("password")
    }
    
    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY] // поток для того чтобы получать новое значение при сохранение/удалении токена
    }
    
    val credentials: Flow<Pair<String?, String?>> = context.dataStore.data.map { preferences ->
        Pair(preferences[USERNAME_KEY], preferences[PASSWORD_KEY])
    }
    
    val isLoggedIn: Flow<Boolean> = token.map { it != null } // собсвенно сам флаг чтобы все знали есть ли токен
    
    suspend fun saveToken(token: String) { // после логина мы сохраняем токен
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }
    
    suspend fun saveCredentials(username: String, password: String) { // ну и сохраняем логин и пароль
        context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
            preferences[PASSWORD_KEY] = password
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
