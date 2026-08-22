package com.example.focusflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflow.data.AppDatabase
import com.example.focusflow.data.model.SessionEntity
import com.example.focusflow.data.repository.SessionRepository
import com.example.focusflow.utils.Pomodoro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyActivity(
    val date: Long,
    val dayLabel: String,
    val workMinutes: Float,
    val workSessions: Int
)

data class StatsState(
    val streak: Int = 0,
    val bestDay: String = "0 мин",
    val avgPerDay: String = "0 мин",
    val totalTime: String = "0 мин",
    val totalSessions: Int = 0,
    val completedPomodoros: Int = 0,
    val focusMinutes: Int = 0,
    val tasksDone: Int = 0,
    val earlyBirds: Int = 0,
    val nightOwls: Int = 0,
    val deepFocusCount: Int = 0,
    val pomodorosToday: Int = 0,
    val weekActivity: List<DailyActivity> = emptyList(),
    val recentSessions: List<SessionEntity> = emptyList(),
    val taskTitles: Map<Long, String> = emptyMap(),
    val weekDelta: Int? = null,
    val sessionsDelta: Int? = null,
    val monthDelta: Int? = null,
    val isLoading: Boolean = true
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionRepository: SessionRepository
    private val db: AppDatabase

    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    init {
        db = AppDatabase.getDatabase(application)
        sessionRepository = SessionRepository(db.sessionDao())
        
        // Подписка на изменения в БД — автоматическое обновление статистики
        viewModelScope.launch {
            sessionRepository.allSessions
                .debounce(300) // Защита от частых обновлений
                .collect { loadStats() }
        }
    }

    fun refresh() = loadStats()

    private fun fmtMin(f: Float): String = Pomodoro.formatMinutes(f)
    private fun delta(cur: Float, prev: Float): Int? = Pomodoro.deltaPct(cur, prev)

    fun formatMinutes(f: Float): String = Pomodoro.formatMinutes(f)
    fun formatDuration(totalSec: Int): String = Pomodoro.formatDuration(totalSec)

    private fun loadStats() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val allSessions = sessionRepository.getRecentSessions(10000)
            val workSessions = allSessions.filter { it.type == "work" }
            val completedPomodoros = workSessions.count { it.isCompleted }

            val totalSeconds = workSessions.sumOf { it.durationSec }
            val focusMinutes = totalSeconds / 60
            val totalTime = if (totalSeconds >= 3600) {
                "${fmtMin(totalSeconds / 3600f)} ч"
            } else {
                "${fmtMin(totalSeconds / 60f)} мин"
            }

            val tasksDone = db.taskDao().getAllTasks().first().count { it.isDone }
        
        // Подсчёт ранних/ночных сессий и чистого фокуса
        val earlyBirds = workSessions.count { s ->
            val hour = Calendar.getInstance().apply { timeInMillis = s.startedAt }.get(Calendar.HOUR_OF_DAY)
            hour < 7
        }
        val nightOwls = workSessions.count { s ->
            val hour = Calendar.getInstance().apply { timeInMillis = s.startedAt }.get(Calendar.HOUR_OF_DAY)
            hour >= 23
        }
        val deepFocusCount = workSessions.count { it.isCompleted }
        
        // Помидоров сегодня
        val todayCal = Calendar.getInstance()
        todayCal.set(Calendar.HOUR_OF_DAY, 0)
        todayCal.set(Calendar.MINUTE, 0)
        todayCal.set(Calendar.SECOND, 0)
        todayCal.set(Calendar.MILLISECOND, 0)
        val pomodorosToday = workSessions.count { it.startedAt >= todayCal.timeInMillis }

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -89)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val dow = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
            calendar.add(Calendar.DAY_OF_YEAR, -dow)
            val startDate = calendar.timeInMillis

            val dayFormat = SimpleDateFormat("EEE", Locale("ru"))
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("ru"))

            val sessionsByDay = workSessions
                .filter { it.startedAt >= startDate }
                .groupBy { dateFormat.format(Date(it.startedAt)) }

            val weekActivity = mutableListOf<DailyActivity>()
            val tempCal = Calendar.getInstance()
            tempCal.timeInMillis = startDate

            while (tempCal.timeInMillis <= System.currentTimeMillis()) {
                val key = dateFormat.format(Date(tempCal.timeInMillis))
                val daySessions = sessionsByDay[key] ?: emptyList()
                val minutes = daySessions.sumOf { it.durationSec } / 60f

                weekActivity.add(
                    DailyActivity(
                        date = tempCal.timeInMillis,
                        dayLabel = dayFormat.format(Date(tempCal.timeInMillis))
                            .take(2).replaceFirstChar { it.uppercase() },
                        workMinutes = minutes,
                        workSessions = daySessions.size
                    )
                )
                tempCal.add(Calendar.DAY_OF_YEAR, 1)
            }

            val last7 = weekActivity.takeLast(7)
            val bestDay = last7.maxOfOrNull { it.workMinutes } ?: 0f
            val avg = if (last7.isNotEmpty()) {
                last7.fold(0f) { acc, d -> acc + d.workMinutes } / last7.size
            } else 0f

            val prev7 = weekActivity.dropLast(7).takeLast(7)
            val curW = last7.fold(0f) { a, d -> a + d.workMinutes }
            val prevW = prev7.fold(0f) { a, d -> a + d.workMinutes }
            val curS = last7.fold(0) { a, d -> a + d.workSessions }
            val prevS = prev7.fold(0) { a, d -> a + d.workSessions }
            val last30 = weekActivity.takeLast(30)
            val prev30 = weekActivity.dropLast(30).takeLast(30)
            val curM = last30.fold(0f) { a, d -> a + d.workMinutes }
            val prevM = prev30.fold(0f) { a, d -> a + d.workMinutes }

            var streak = 0
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            while (true) {
                val key = dateFormat.format(Date(cal.timeInMillis))
                if (sessionsByDay[key]?.isNotEmpty() == true) {
                    streak++
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                } else break
            }

            val taskIds = allSessions.mapNotNull { it.taskId }.distinct()
            val taskTitles = mutableMapOf<Long, String>()
            for (id in taskIds) {
                db.taskDao().getTaskById(id)?.let { taskTitles[id] = it.title }
            }

            _state.value = StatsState(
            streak = streak,
            bestDay = "${fmtMin(bestDay)} мин",
            avgPerDay = "${fmtMin(avg)} мин",
            totalTime = totalTime,
            totalSessions = workSessions.size,
            completedPomodoros = completedPomodoros,
            focusMinutes = focusMinutes,
            tasksDone = tasksDone,
            earlyBirds = earlyBirds,
            nightOwls = nightOwls,
            deepFocusCount = deepFocusCount,
            pomodorosToday = pomodorosToday,
                weekActivity = weekActivity,
                recentSessions = allSessions.take(50),
                taskTitles = taskTitles,
                weekDelta = delta(curW, prevW),
                sessionsDelta = delta(curS.toFloat(), prevS.toFloat()),
                monthDelta = delta(curM, prevM),
                isLoading = false
            )
        }
    }
}