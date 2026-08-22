package com.example.focusflow.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val WORK_DURATION = intPreferencesKey("work_duration")
        val WORK_SECONDS = intPreferencesKey("work_seconds")
        val SHORT_BREAK_DURATION = intPreferencesKey("short_break_duration")
        val SHORT_BREAK_SECONDS = intPreferencesKey("short_break_seconds")
        val LONG_BREAK_DURATION = intPreferencesKey("long_break_duration")
        val LONG_BREAK_SECONDS = intPreferencesKey("long_break_seconds")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val SOUND_VOLUME = floatPreferencesKey("sound_volume")
        val SOUND_TYPE = stringPreferencesKey("sound_type")
        val AUTO_START = booleanPreferencesKey("auto_start")
        val THEME = stringPreferencesKey("theme")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val SESSIONS_UNTIL_LONG_BREAK = intPreferencesKey("sessions_until_long_break")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val PREMIUM_CODE = stringPreferencesKey("premium_code")
        val PREMIUM_EXPIRES_AT = longPreferencesKey("premium_expires_at")
        val AMBIENT_TYPE = stringPreferencesKey("ambient_type")
        val AMBIENT_VOLUME = floatPreferencesKey("ambient_volume")
        val STRICT_MODE = booleanPreferencesKey("strict_mode")
    }

    val workDuration: Flow<Int> = context.dataStore.data.map { it[PreferencesKeys.WORK_DURATION] ?: 25 }
    val workSeconds: Flow<Int> = context.dataStore.data.map { it[PreferencesKeys.WORK_SECONDS] ?: 0 }
    val shortBreakDuration: Flow<Int> = context.dataStore.data.map { it[PreferencesKeys.SHORT_BREAK_DURATION] ?: 5 }
    val shortBreakSeconds: Flow<Int> = context.dataStore.data.map { it[PreferencesKeys.SHORT_BREAK_SECONDS] ?: 0 }
    val longBreakDuration: Flow<Int> = context.dataStore.data.map { it[PreferencesKeys.LONG_BREAK_DURATION] ?: 15 }
    val longBreakSeconds: Flow<Int> = context.dataStore.data.map { it[PreferencesKeys.LONG_BREAK_SECONDS] ?: 0 }
    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.SOUND_ENABLED] ?: true }
    val soundVolume: Flow<Float> = context.dataStore.data.map { it[PreferencesKeys.SOUND_VOLUME] ?: 1.0f }
    val soundType: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.SOUND_TYPE] ?: "bell" }
    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.VIBRATION_ENABLED] ?: true }
    val autoStart: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.AUTO_START] ?: false }
    val theme: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.THEME] ?: "dark" }
    val dailyGoal: Flow<Int> = context.dataStore.data.map { it[PreferencesKeys.DAILY_GOAL] ?: 8 }
    val sessionsUntilLongBreak: Flow<Int> = context.dataStore.data.map { it[PreferencesKeys.SESSIONS_UNTIL_LONG_BREAK] ?: 4 }
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.ONBOARDING_COMPLETED] ?: false }
    val isPremium: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.IS_PREMIUM] ?: false }
    val premiumExpiresAt: Flow<Long> = context.dataStore.data.map { it[PreferencesKeys.PREMIUM_EXPIRES_AT] ?: 0L }
    val ambientType: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.AMBIENT_TYPE] ?: "off" }
    val ambientVolume: Flow<Float> = context.dataStore.data.map { it[PreferencesKeys.AMBIENT_VOLUME] ?: 0.5f }
    val strictMode: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.STRICT_MODE] ?: false }

    suspend fun saveWorkDuration(v: Int) = context.dataStore.edit { it[PreferencesKeys.WORK_DURATION] = v }
    suspend fun saveWorkSeconds(v: Int) = context.dataStore.edit { it[PreferencesKeys.WORK_SECONDS] = v }
    suspend fun saveShortBreakDuration(v: Int) = context.dataStore.edit { it[PreferencesKeys.SHORT_BREAK_DURATION] = v }
    suspend fun saveShortBreakSeconds(v: Int) = context.dataStore.edit { it[PreferencesKeys.SHORT_BREAK_SECONDS] = v }
    suspend fun saveLongBreakDuration(v: Int) = context.dataStore.edit { it[PreferencesKeys.LONG_BREAK_DURATION] = v }
    suspend fun saveLongBreakSeconds(v: Int) = context.dataStore.edit { it[PreferencesKeys.LONG_BREAK_SECONDS] = v }
    suspend fun saveSoundEnabled(v: Boolean) = context.dataStore.edit { it[PreferencesKeys.SOUND_ENABLED] = v }
    suspend fun saveSoundVolume(v: Float) = context.dataStore.edit { it[PreferencesKeys.SOUND_VOLUME] = v }
    suspend fun saveSoundType(v: String) = context.dataStore.edit { it[PreferencesKeys.SOUND_TYPE] = v }
    suspend fun saveVibrationEnabled(v: Boolean) = context.dataStore.edit { it[PreferencesKeys.VIBRATION_ENABLED] = v }
    suspend fun saveAutoStart(v: Boolean) = context.dataStore.edit { it[PreferencesKeys.AUTO_START] = v }
    suspend fun saveTheme(v: String) = context.dataStore.edit { it[PreferencesKeys.THEME] = v }
    suspend fun saveDailyGoal(v: Int) = context.dataStore.edit { it[PreferencesKeys.DAILY_GOAL] = v }
    suspend fun saveSessionsUntilLongBreak(v: Int) = context.dataStore.edit { it[PreferencesKeys.SESSIONS_UNTIL_LONG_BREAK] = v }
    suspend fun saveOnboardingCompleted(v: Boolean) = context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = v }
    suspend fun saveIsPremium(v: Boolean) = context.dataStore.edit { it[PreferencesKeys.IS_PREMIUM] = v }
    suspend fun savePremiumCode(v: String) = context.dataStore.edit { it[PreferencesKeys.PREMIUM_CODE] = v }
    suspend fun savePremiumExpiresAt(v: Long) = context.dataStore.edit { it[PreferencesKeys.PREMIUM_EXPIRES_AT] = v }
    suspend fun saveAmbientType(v: String) = context.dataStore.edit { it[PreferencesKeys.AMBIENT_TYPE] = v }
    suspend fun saveAmbientVolume(v: Float) = context.dataStore.edit { it[PreferencesKeys.AMBIENT_VOLUME] = v }
    suspend fun saveStrictMode(v: Boolean) = context.dataStore.edit { it[PreferencesKeys.STRICT_MODE] = v }

    suspend fun getWorkDurationSeconds(): Int = workDuration.first() * 60 + workSeconds.first()
    suspend fun getShortBreakDurationSeconds(): Int = shortBreakDuration.first() * 60 + shortBreakSeconds.first()
    suspend fun getLongBreakDurationSeconds(): Int = longBreakDuration.first() * 60 + longBreakSeconds.first()
}