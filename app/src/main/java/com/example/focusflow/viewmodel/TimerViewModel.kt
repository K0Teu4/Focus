package com.example.focusflow.viewmodel

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflow.data.AppDatabase
import com.example.focusflow.data.model.Categories
import com.example.focusflow.data.model.SessionType
import com.example.focusflow.data.model.TaskEntity
import com.example.focusflow.data.repository.SessionRepository
import com.example.focusflow.data.repository.SettingsRepository
import com.example.focusflow.data.repository.TaskRepository
import com.example.focusflow.services.TimerNotificationHub
import com.example.focusflow.services.TimerService
import com.example.focusflow.sound.AmbientSoundManager
import com.example.focusflow.sound.SoundManager
import com.example.focusflow.sound.VibrationService
import com.example.focusflow.utils.CatAchievement
import com.example.focusflow.utils.CatGarden
import com.example.focusflow.utils.Pomodoro
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

data class TimerState(
    val timeRemaining: Int = 25 * 60,
    val totalTime: Int = 25 * 60,
    val isRunning: Boolean = false,
    val sessionType: SessionType = SessionType.WORK,
    val completedPomodoros: Int = 0,
    val currentTaskId: Long? = null,
    val currentTaskCategory: String = Categories.WORK,
    val snackbarMessage: String? = null,
    val autoStartCountdown: Int = 0,
    val isAutoStartCounting: Boolean = false,
    val dailyProgress: Int = 0,
    val dailyGoal: Int = 8,
    val strictMode: Boolean = false
)

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val taskRepository: TaskRepository
    private val sessionRepository: SessionRepository
    private val settingsRepository = SettingsRepository(application)
    private val soundManager = SoundManager(application)
    private val vibrationService = VibrationService(application)

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasks: StateFlow<List<TaskEntity>> = _tasks.asStateFlow()

    private val _sessionsUntilLong = MutableStateFlow(4)
    val sessionsUntilLong: StateFlow<Int> = _sessionsUntilLong.asStateFlow()

    private val _taskCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val taskCounts: StateFlow<Map<Long, Int>> = _taskCounts.asStateFlow()

    private var timerJob: Job? = null
    private var autoStartJob: Job? = null
    @Volatile private var sessionStartedAt: Long = 0L
    @Volatile private var sessionWorkedSec: Int = 0

    init {
        val db = AppDatabase.getDatabase(application)
        taskRepository = TaskRepository(db.taskDao())
        sessionRepository = SessionRepository(db.sessionDao())
        AmbientSoundManager.init(application)

        TimerNotificationHub.actionListener = { action ->
            when (action) {
                "toggle" -> if (_timerState.value.isRunning) pauseTimer() else startTimer()
                "skip" -> skipSession()
            }
        }

        viewModelScope.launch { taskRepository.allTasks.collect { _tasks.value = it } }

        viewModelScope.launch {
            sessionRepository.allSessions.collect { sessions ->
                _taskCounts.value = sessions
                    .filter { it.type == "work" && it.isCompleted && it.taskId != null }
                    .groupingBy { it.taskId!! }
                    .eachCount()
            }
        }

        viewModelScope.launch {
            val duration = settingsRepository.getWorkDurationSeconds()
            val dailyGoal = settingsRepository.dailyGoal.first()
            val strict = settingsRepository.strictMode.first()
            _timerState.value = _timerState.value.copy(
                timeRemaining = duration,
                totalTime = duration,
                dailyGoal = dailyGoal,
                strictMode = strict
            )
        }

        viewModelScope.launch { settingsRepository.workDuration.collect { updateTimerFromSettings() } }

        viewModelScope.launch {
            settingsRepository.dailyGoal.collect { goal ->
                _timerState.value = _timerState.value.copy(dailyGoal = goal)
            }
        }

        viewModelScope.launch {
            settingsRepository.sessionsUntilLongBreak.collect { n ->
                _sessionsUntilLong.value = n
            }
        }

        viewModelScope.launch {
            settingsRepository.strictMode.collect { enabled ->
                _timerState.value = _timerState.value.copy(strictMode = enabled)
            }
        }

        viewModelScope.launch {
            _timerState.collect { st ->
                TimerNotificationHub.remainingSeconds.value = st.timeRemaining
                TimerNotificationHub.totalSeconds.value = st.totalTime.coerceAtLeast(1)
                TimerNotificationHub.isRunning.value = st.isRunning
                TimerNotificationHub.sessionLabel.value = when (st.sessionType) {
                    SessionType.WORK -> "Фокус"
                    SessionType.SHORT_BREAK -> "Короткий перерыв"
                    SessionType.LONG_BREAK -> "Длинный перерыв"
                }
            }
        }

        viewModelScope.launch { loadDailyProgress() }
    }

    private suspend fun loadDailyProgress() {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val sessions = sessionRepository.getRecentSessions(1000)
        val todaySessions = sessions.count {
            it.startedAt >= today && it.type == "work" && it.isCompleted
        }
        _timerState.value = _timerState.value.copy(dailyProgress = todaySessions)
    }

    private suspend fun updateTimerFromSettings() {
        val st = _timerState.value
        if (!st.isRunning) {
            val duration = when (st.sessionType) {
                SessionType.WORK -> settingsRepository.getWorkDurationSeconds()
                SessionType.SHORT_BREAK -> settingsRepository.getShortBreakDurationSeconds()
                SessionType.LONG_BREAK -> settingsRepository.getLongBreakDurationSeconds()
            }
            _timerState.value = st.copy(timeRemaining = duration, totalTime = duration)
        }
    }

    private fun ensureServiceStarted() {
        val ctx = getApplication<Application>()
        ContextCompat.startForegroundService(ctx, Intent(ctx, TimerService::class.java))
    }

    private fun stopTimerService() {
        getApplication<Application>().stopService(
            Intent(getApplication(), TimerService::class.java)
        )
    }

    private fun startAmbientIfEnabled() {
        viewModelScope.launch {
            val isPremium = settingsRepository.isPremium.first()
            val type = settingsRepository.ambientType.first()
            if (isPremium && type != "off") {
                AmbientSoundManager.setVolume(settingsRepository.ambientVolume.first())
                AmbientSoundManager.start(type)
            } else {
                AmbientSoundManager.stop()
            }
        }
    }

    private fun stopAmbient() {
        AmbientSoundManager.stop()
    }

    fun addTask(title: String, category: String = Categories.WORK) {
        viewModelScope.launch { taskRepository.insert(TaskEntity(title = title, category = category)) }
    }

    fun updateTask(task: TaskEntity) { viewModelScope.launch { taskRepository.update(task) } }
    fun deleteTask(task: TaskEntity) { viewModelScope.launch { taskRepository.delete(task) } }
    suspend fun restoreTask(task: TaskEntity) { taskRepository.insert(task) }
    fun setTaskDone(taskId: Long, isDone: Boolean) {
        viewModelScope.launch { taskRepository.setTaskDone(taskId, isDone) }
    }

    fun focusOnTask(task: TaskEntity?) {
        cancelAutoStartCountdown()
        timerJob?.cancel()

        if (task == null) {
            viewModelScope.launch {
                val workDuration = settingsRepository.getWorkDurationSeconds()
                _timerState.value = TimerState(
                    timeRemaining = workDuration, totalTime = workDuration,
                    isRunning = false, sessionType = SessionType.WORK,
                    completedPomodoros = _timerState.value.completedPomodoros,
                    currentTaskId = null, currentTaskCategory = Categories.WORK,
                    dailyProgress = _timerState.value.dailyProgress,
                    dailyGoal = _timerState.value.dailyGoal,
                    strictMode = _timerState.value.strictMode
                )
            }
            return
        }

        val isWorkSession = task.category in listOf(Categories.WORK, Categories.STUDY)
        val sessionType = if (isWorkSession) SessionType.WORK else SessionType.SHORT_BREAK

        viewModelScope.launch {
            val duration = getDurationForSession(sessionType)
            _timerState.value = TimerState(
                timeRemaining = duration, totalTime = duration,
                isRunning = false, sessionType = sessionType,
                completedPomodoros = _timerState.value.completedPomodoros,
                currentTaskId = task.id, currentTaskCategory = task.category,
                dailyProgress = _timerState.value.dailyProgress,
                dailyGoal = _timerState.value.dailyGoal,
                strictMode = _timerState.value.strictMode
            )
        }
    }

    fun switchToWork() {
        if (_timerState.value.isRunning) return
        viewModelScope.launch {
            val d = settingsRepository.getWorkDurationSeconds()
            _timerState.value = _timerState.value.copy(
                timeRemaining = d, totalTime = d, sessionType = SessionType.WORK
            )
        }
    }

    fun switchToBreak() {
        if (_timerState.value.isRunning) return
        viewModelScope.launch {
            val d = settingsRepository.getShortBreakDurationSeconds()
            _timerState.value = _timerState.value.copy(
                timeRemaining = d, totalTime = d, sessionType = SessionType.SHORT_BREAK
            )
        }
    }

    fun switchToLongBreak() {
        if (_timerState.value.isRunning) return
        viewModelScope.launch {
            val d = settingsRepository.getLongBreakDurationSeconds()
            _timerState.value = _timerState.value.copy(
                timeRemaining = d, totalTime = d, sessionType = SessionType.LONG_BREAK
            )
        }
    }

    fun startTimer() {
        if (_timerState.value.isRunning) return

        cancelAutoStartCountdown()
        ensureServiceStarted()
        startAmbientIfEnabled()
        sessionStartedAt = System.currentTimeMillis()
        sessionWorkedSec = _timerState.value.totalTime - _timerState.value.timeRemaining
        _timerState.value = _timerState.value.copy(isRunning = true)

        timerJob = viewModelScope.launch {
            while (_timerState.value.timeRemaining > 0 && _timerState.value.isRunning) {
                delay(1000)
                sessionWorkedSec++
                _timerState.value = _timerState.value.copy(
                    timeRemaining = _timerState.value.timeRemaining - 1
                )
            }
            if (_timerState.value.timeRemaining == 0) onTimerComplete()
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        stopAmbient()
        if (_timerState.value.strictMode &&
            _timerState.value.sessionType == SessionType.WORK &&
            sessionWorkedSec > 0) {
            burnPomodoro("🔥 Помидор сгорел: пауза в строгом режиме")
            return
        }
        _timerState.value = _timerState.value.copy(isRunning = false)
    }

    fun resetTimer() {
        timerJob?.cancel()
        cancelAutoStartCountdown()
        stopAmbient()
        stopTimerService()

        if (_timerState.value.strictMode &&
            _timerState.value.sessionType == SessionType.WORK &&
            sessionWorkedSec > 0) {
            burnPomodoro("🔥 Помидор сгорел: сброс в строгом режиме")
            return
        }

        viewModelScope.launch {
            val duration = getDurationForSession(_timerState.value.sessionType)
            _timerState.value = _timerState.value.copy(
                timeRemaining = duration, totalTime = duration, isRunning = false
            )
            sessionWorkedSec = 0
        }
    }

    fun skipSession() {
        val st = _timerState.value
        timerJob?.cancel()
        stopAmbient()

        if (st.strictMode && st.sessionType == SessionType.WORK && sessionWorkedSec > 0) {
            burnPomodoro("🔥 Помидор сгорел: пропуск в строгом режиме")
            return
        }

        val elapsed = st.totalTime - st.timeRemaining
        if (elapsed > 0) saveCurrentSession(elapsed, isCompleted = false)

        val nextType = getNextSessionType()
        viewModelScope.launch {
            val nextDuration = getDurationForSession(nextType)
            _timerState.value = st.copy(
                timeRemaining = nextDuration, totalTime = nextDuration,
                isRunning = false, sessionType = nextType,
                snackbarMessage = if (elapsed > 0) "⏭ Сохранено: ${formatDuration(elapsed)}" else "⏭ Сессия пропущена"
            )
            sessionWorkedSec = 0
        }
    }

    private fun burnPomodoro(message: String) {
        val st = _timerState.value
        saveCurrentSession(sessionWorkedSec, isCompleted = false)

        val nextType = if (st.sessionType == SessionType.WORK) {
            if ((st.completedPomodoros + 1) % _sessionsUntilLong.value == 0) {
                SessionType.LONG_BREAK
            } else SessionType.SHORT_BREAK
        } else SessionType.WORK

        viewModelScope.launch {
            val nextDuration = getDurationForSession(nextType)
            _timerState.value = st.copy(
                timeRemaining = nextDuration, totalTime = nextDuration,
                isRunning = false, sessionType = nextType,
                snackbarMessage = message
            )
            sessionWorkedSec = 0
        }
    }

    fun burnPomodoroOnEmergencyExit() {
        val st = _timerState.value
        if (st.strictMode && st.sessionType == SessionType.WORK && sessionWorkedSec > 0) {
            burnPomodoro("🔥 Помидор сгорел: выход в строгом режиме")
        }
    }

    private fun getNextSessionType(): SessionType {
        val st = _timerState.value
        return if (st.sessionType == SessionType.WORK) {
            val newCount = st.completedPomodoros + 1
            if (newCount % _sessionsUntilLong.value == 0) SessionType.LONG_BREAK else SessionType.SHORT_BREAK
        } else SessionType.WORK
    }

    private suspend fun getDurationForSession(type: SessionType): Int = when (type) {
        SessionType.WORK -> settingsRepository.getWorkDurationSeconds()
        SessionType.SHORT_BREAK -> settingsRepository.getShortBreakDurationSeconds()
        SessionType.LONG_BREAK -> settingsRepository.getLongBreakDurationSeconds()
    }

    private fun saveCurrentSession(durationSec: Int, isCompleted: Boolean) {
        val st = _timerState.value
        val type = when (st.sessionType) {
            SessionType.WORK -> "work"
            SessionType.SHORT_BREAK -> "short_break"
            SessionType.LONG_BREAK -> "long_break"
        }
        viewModelScope.launch {
            sessionRepository.saveSession(
                taskId = st.currentTaskId, type = type,
                durationSec = durationSec, isCompleted = isCompleted
            )
        }
    }

    private fun onTimerComplete() {
        val st = _timerState.value
        stopAmbient()

        viewModelScope.launch {
            val soundEnabled = settingsRepository.soundEnabled.first()
            val volume = settingsRepository.soundVolume.first()
            val vibrationEnabled = settingsRepository.vibrationEnabled.first()

            soundManager.setVolume(volume)
            if (soundEnabled) {
                soundManager.playMultiple(settingsRepository.soundType.first(), 3, 1500)
            }
            if (vibrationEnabled) {
                vibrationService.vibrate(VibrationService.VibrationPattern.COMPLETION)
            }
        }

        saveCurrentSession(st.totalTime, isCompleted = true)

        val finishMessage = when (st.sessionType) {
            SessionType.WORK -> "Работа завершена!"
            SessionType.SHORT_BREAK -> "Короткий перерыв завершён!"
            SessionType.LONG_BREAK -> "Длинный перерыв завершён!"
        }

        if (st.sessionType == SessionType.WORK) {
            val newCount = st.completedPomodoros + 1
            val newDailyProgress = st.dailyProgress + 1

            // 🎉 Празднование нового кота за помидоры
            val celebration = CatGarden.achievements
                .firstOrNull {
                    it.kind == CatAchievement.Kind.POMODOROS && it.threshold == newCount
                }
                ?.let { "🎉 Новый кот: ${it.emoji} ${it.name}!" }

            st.currentTaskId?.let { taskId ->
                viewModelScope.launch { taskRepository.getTaskById(taskId)?.let { taskRepository.update(it) } }
            }

            val nextSession = Pomodoro.nextSessionType(true, st.completedPomodoros, _sessionsUntilLong.value)

            viewModelScope.launch {
                val nextDuration = getDurationForSession(nextSession)
                _timerState.value = st.copy(
                    timeRemaining = nextDuration, totalTime = nextDuration,
                    isRunning = false, sessionType = nextSession,
                    completedPomodoros = newCount,
                    snackbarMessage = celebration ?: finishMessage,
                    dailyProgress = newDailyProgress
                )
                sessionWorkedSec = 0
                checkAutoStart()
            }
        } else {
            viewModelScope.launch {
                val workDuration = settingsRepository.getWorkDurationSeconds()
                _timerState.value = st.copy(
                    timeRemaining = workDuration, totalTime = workDuration,
                    isRunning = false, sessionType = SessionType.WORK,
                    snackbarMessage = finishMessage
                )
                sessionWorkedSec = 0
                checkAutoStart()
            }
        }
    }

    private fun checkAutoStart() {
        viewModelScope.launch {
            if (settingsRepository.autoStart.first()) startAutoStartCountdown(3)
        }
    }

    fun startAutoStartCountdown(seconds: Int) {
        cancelAutoStartCountdown()
        _timerState.value = _timerState.value.copy(isAutoStartCounting = true, autoStartCountdown = seconds)

        autoStartJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                _timerState.value = _timerState.value.copy(autoStartCountdown = i)
                delay(1000)
            }
            _timerState.value = _timerState.value.copy(isAutoStartCounting = false, autoStartCountdown = 0)
            startTimer()
        }
    }

    fun cancelAutoStartCountdown() {
        autoStartJob?.cancel()
        _timerState.value = _timerState.value.copy(isAutoStartCounting = false, autoStartCountdown = 0)
    }

    fun clearSnackbar() {
        _timerState.value = _timerState.value.copy(snackbarMessage = null)
    }

    fun formatTime(seconds: Int): String = Pomodoro.formatTime(seconds)
    fun formatDuration(totalSec: Int): String = Pomodoro.formatDuration(totalSec)

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        autoStartJob?.cancel()
        soundManager.release()
        stopTimerService()
        stopAmbient()
    }

    // ===== Dev Tools (только для разработчика) =====
    fun devSeedPomodoros(count: Int) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val perDay = (count / 7).coerceAtLeast(1)
        repeat(count) { i ->
            val dayOffset = (i / perDay).coerceAtMost(6)
            val startTime = now - (dayOffset * 24L * 60 * 60 * 1000) - (i * 30 * 60 * 1000L)
            sessionRepository.saveSession(
                taskId = null,
                type = "work",
                durationSec = 1500,
                isCompleted = true
            )
        }
    }

    fun devSeedStreak(days: Int) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        repeat(days) { d ->
            val startTime = now - (d * 24L * 60 * 60 * 1000) - (12 * 60 * 60 * 1000L)
            sessionRepository.saveSession(
                taskId = null,
                type = "work",
                durationSec = 1500,
                isCompleted = true
            )
        }
    }

    fun devSeedTasks(count: Int) = viewModelScope.launch {
        repeat(count) { i ->
            val task = TaskEntity(
                id = 0,
                title = "Задача ${i + 1}",
                category = Categories.WORK,
                isDone = i < count / 2
            )
            taskRepository.insert(task)
        }
    }

    fun devResetStats() = viewModelScope.launch {
        // Сброс через удаление всех записей
        val allSessions = sessionRepository.getRecentSessions(10000)
        // Удаляем задачи и сессии через репозитории
    }
}
