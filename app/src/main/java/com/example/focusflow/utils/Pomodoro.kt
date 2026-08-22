package com.example.focusflow.utils

import com.example.focusflow.data.model.SessionType
import java.util.Locale
import kotlin.math.roundToInt

/** Чистые функции без Android-зависимостей — покрыты unit-тестами */
object Pomodoro {

    fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }

    fun formatDuration(totalSec: Int): String {
        val m = totalSec / 60
        val s = totalSec % 60
        return if (s > 0) "$m мин $s сек" else "$m мин"
    }

    fun formatMinutes(f: Float): String {
        val s = String.format(Locale.US, "%.1f", f)
        return if (s.endsWith(".0")) s.dropLast(2) else s
    }

    fun deltaPct(cur: Float, prev: Float): Int? = when {
        prev > 0f -> ((cur - prev) / prev * 100f).roundToInt()
        cur > 0f -> 100
        else -> null
    }

    fun nextSessionType(currentIsWork: Boolean, completedBefore: Int, sessionsUntilLong: Int): SessionType {
        if (!currentIsWork) return SessionType.WORK
        return if ((completedBefore + 1) % sessionsUntilLong == 0) {
            SessionType.LONG_BREAK
        } else {
            SessionType.SHORT_BREAK
        }
    }
}