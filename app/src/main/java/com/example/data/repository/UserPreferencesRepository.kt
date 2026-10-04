package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class ReaderSettings(
    val fontSizeSp: Float = 17f,
    val lineSpacingMultiplier: Float = 1.6f,
    val readerTheme: String = "system" // system, light, dark, sepia
)

data class AppPreferences(
    val themeMode: String = "system", // system, light, dark
    val readerSettings: ReaderSettings = ReaderSettings(),
    val dailyReminderEnabled: Boolean = true,
    val testTimerDurationMinutes: Int = 15,
    val isInitialized: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
        private val FONT_SIZE_KEY = floatPreferencesKey("reader_font_size")
        private val LINE_SPACING_KEY = floatPreferencesKey("reader_line_spacing")
        private val READER_THEME_KEY = stringPreferencesKey("reader_theme")
        private val REMINDER_KEY = booleanPreferencesKey("daily_reminder")
        private val TIMER_KEY = intPreferencesKey("test_timer_minutes")
        private val INITIALIZED_KEY = booleanPreferencesKey("app_initialized")
    }

    val preferencesFlow: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        AppPreferences(
            themeMode = prefs[THEME_KEY] ?: "system",
            readerSettings = ReaderSettings(
                fontSizeSp = prefs[FONT_SIZE_KEY] ?: 17f,
                lineSpacingMultiplier = prefs[LINE_SPACING_KEY] ?: 1.6f,
                readerTheme = prefs[READER_THEME_KEY] ?: "system"
            ),
            dailyReminderEnabled = prefs[REMINDER_KEY] ?: true,
            testTimerDurationMinutes = prefs[TIMER_KEY] ?: 15,
            isInitialized = prefs[INITIALIZED_KEY] ?: false
        )
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_KEY] = mode }
    }

    suspend fun setReaderFontSize(sizeSp: Float) {
        context.dataStore.edit { it[FONT_SIZE_KEY] = sizeSp }
    }

    suspend fun setReaderLineSpacing(multiplier: Float) {
        context.dataStore.edit { it[LINE_SPACING_KEY] = multiplier }
    }

    suspend fun setReaderTheme(theme: String) {
        context.dataStore.edit { it[READER_THEME_KEY] = theme }
    }

    suspend fun setDailyReminder(enabled: Boolean) {
        context.dataStore.edit { it[REMINDER_KEY] = enabled }
    }

    suspend fun setTestTimerDuration(minutes: Int) {
        context.dataStore.edit { it[TIMER_KEY] = minutes }
    }

    suspend fun setInitialized(initialized: Boolean) {
        context.dataStore.edit { it[INITIALIZED_KEY] = initialized }
    }
}
