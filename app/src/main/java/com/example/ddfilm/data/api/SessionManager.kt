package com.example.ddfilm.data.api

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {
    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val COINS_REGULAR = intPreferencesKey("coins_regular")
        private val COINS_BONUS = intPreferencesKey("coins_bonus")
        private val THEME_MODE = stringPreferencesKey("theme_mode") // "light", "dark", "system"
        private val LANGUAGE = stringPreferencesKey("language") // "id", "en"
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN] }
    val coinsRegular: Flow<Int> = context.dataStore.data.map { it[COINS_REGULAR] ?: 0 }
    val coinsBonus: Flow<Int> = context.dataStore.data.map { it[COINS_BONUS] ?: 0 }
    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "system" }
    val language: Flow<String> = context.dataStore.data.map { it[LANGUAGE] ?: "id" }

    suspend fun saveSession(accessToken: String, refreshToken: String, userName: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
            preferences[USER_NAME] = userName
        }
    }

    suspend fun updateWallet(regular: Int, bonus: Int) {
        context.dataStore.edit { preferences ->
            preferences[COINS_REGULAR] = regular
            preferences[COINS_BONUS] = bonus
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = lang
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
