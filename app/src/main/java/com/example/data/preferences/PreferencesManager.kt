package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rudra_settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val KEY_SELECTED_BOARD = stringPreferencesKey("selected_board")
        val KEY_LOW_ENERGY_MODE = booleanPreferencesKey("low_energy_mode")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
        val KEY_DAILY_STREAK = intPreferencesKey("daily_streak")
        val KEY_LAST_ACTIVE_DATE = stringPreferencesKey("last_active_date")
        val KEY_INITIALIZED = booleanPreferencesKey("database_initialized")
    }

    val geminiApiKeyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_GEMINI_API_KEY] ?: run {
            try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }
        }
    }

    val selectedBoardFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_BOARD] ?: "BSEB"
    }

    val isLowEnergyModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_LOW_ENERGY_MODE] ?: false
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: "DARK"
    }

    val isInitializedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_INITIALIZED] ?: false
    }

    val streakFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_DAILY_STREAK] ?: 1
    }

    suspend fun setGeminiApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GEMINI_API_KEY] = key
        }
    }

    suspend fun setSelectedBoard(board: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_BOARD] = board
        }
    }

    suspend fun setLowEnergyMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LOW_ENERGY_MODE] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setInitialized(initialized: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_INITIALIZED] = initialized
        }
    }

    suspend fun updateStreak(todayDate: String) {
        context.dataStore.edit { preferences ->
            val lastDate = preferences[KEY_LAST_ACTIVE_DATE] ?: ""
            val currentStreak = preferences[KEY_DAILY_STREAK] ?: 0
            if (lastDate != todayDate) {
                // If it was yesterday, increment, else if fresh or skipped more than 1 day reset or maintain
                preferences[KEY_DAILY_STREAK] = (currentStreak + 1).coerceAtLeast(1)
                preferences[KEY_LAST_ACTIVE_DATE] = todayDate
            }
        }
    }
}
