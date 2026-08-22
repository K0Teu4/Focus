package com.example.focusflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflow.data.repository.SettingsRepository
import com.example.focusflow.sound.AmbientSoundManager
import com.example.focusflow.sound.SoundManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsState(
    val workDuration: Int = 25,
    val workSeconds: Int = 0,
    val shortBreakDuration: Int = 5,
    val shortBreakSeconds: Int = 0,
    val longBreakDuration: Int = 15,
    val longBreakSeconds: Int = 0,
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 1.0f,
    val soundType: String = "bell",
    val vibrationEnabled: Boolean = true,
    val autoStart: Boolean = false,
    val theme: String = "dark",
    val dailyGoal: Int = 8,
    val sessionsUntilLongBreak: Int = 4,
    val ambientType: String = "off",
    val ambientVolume: Float = 0.5f,
    val strictMode: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)
    private val soundManager = SoundManager(application)

    init {
        AmbientSoundManager.init(application)
    }

    val settingsState: StateFlow<SettingsState> = kotlinx.coroutines.flow.combine(
        repository.workDuration,
        repository.workSeconds,
        repository.shortBreakDuration,
        repository.shortBreakSeconds,
        repository.longBreakDuration,
        repository.longBreakSeconds,
        repository.soundEnabled,
        repository.soundVolume,
        repository.soundType,
        repository.vibrationEnabled,
        repository.autoStart,
        repository.theme,
        repository.dailyGoal,
        repository.sessionsUntilLongBreak,
        repository.ambientType,
        repository.ambientVolume,
        repository.strictMode
    ) { values ->
        SettingsState(
            workDuration = values[0] as Int,
            workSeconds = values[1] as Int,
            shortBreakDuration = values[2] as Int,
            shortBreakSeconds = values[3] as Int,
            longBreakDuration = values[4] as Int,
            longBreakSeconds = values[5] as Int,
            soundEnabled = values[6] as Boolean,
            soundVolume = values[7] as Float,
            soundType = values[8] as String,
            vibrationEnabled = values[9] as Boolean,
            autoStart = values[10] as Boolean,
            theme = values[11] as String,
            dailyGoal = values[12] as Int,
            sessionsUntilLongBreak = values[13] as Int,
            ambientType = values[14] as String,
            ambientVolume = values[15] as Float,
            strictMode = values[16] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState()
    )

    /** null = DataStore ещё не прочитан (показываем splash, а не онбординг) */
    val onboardingCompleted: StateFlow<Boolean?> = repository.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun completeOnboarding() = viewModelScope.launch { repository.saveOnboardingCompleted(true) }

    fun updateWorkDuration(v: Int) = viewModelScope.launch { repository.saveWorkDuration(v) }
    fun updateWorkSeconds(v: Int) = viewModelScope.launch { repository.saveWorkSeconds(v) }
    fun updateShortBreakDuration(v: Int) = viewModelScope.launch { repository.saveShortBreakDuration(v) }
    fun updateShortBreakSeconds(v: Int) = viewModelScope.launch { repository.saveShortBreakSeconds(v) }
    fun updateLongBreakDuration(v: Int) = viewModelScope.launch { repository.saveLongBreakDuration(v) }
    fun updateLongBreakSeconds(v: Int) = viewModelScope.launch { repository.saveLongBreakSeconds(v) }
    fun updateSoundEnabled(v: Boolean) = viewModelScope.launch { repository.saveSoundEnabled(v) }
    fun updateSoundVolume(v: Float) = viewModelScope.launch { repository.saveSoundVolume(v) }
    fun updateSoundType(v: String) = viewModelScope.launch { repository.saveSoundType(v) }
    fun updateVibrationEnabled(v: Boolean) = viewModelScope.launch { repository.saveVibrationEnabled(v) }
    fun updateAutoStart(v: Boolean) = viewModelScope.launch { repository.saveAutoStart(v) }
    fun updateTheme(v: String) = viewModelScope.launch { repository.saveTheme(v) }
    fun updateDailyGoal(v: Int) = viewModelScope.launch { repository.saveDailyGoal(v) }
    fun updateSessionsUntilLongBreak(v: Int) = viewModelScope.launch { repository.saveSessionsUntilLongBreak(v) }
    fun updateAmbientType(v: String) = viewModelScope.launch { repository.saveAmbientType(v) }
    fun updateAmbientVolume(v: Float) = viewModelScope.launch { repository.saveAmbientVolume(v) }
    fun updateStrictMode(v: Boolean) = viewModelScope.launch { repository.saveStrictMode(v) }

    fun previewSound(type: String) {
        viewModelScope.launch {
            soundManager.setVolume(repository.soundVolume.first())
            soundManager.play(type)
        }
    }

    fun previewAmbient(type: String) {
        viewModelScope.launch {
            AmbientSoundManager.setVolume(repository.ambientVolume.first())
            if (AmbientSoundManager.isPlaying()) {
                AmbientSoundManager.stop()
            } else {
                AmbientSoundManager.start(type)
            }
        }
    }

    fun stopAmbientPreview() {
        AmbientSoundManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
        AmbientSoundManager.stop()
    }
}