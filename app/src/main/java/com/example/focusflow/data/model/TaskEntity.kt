package com.example.focusflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = "work", // work, rest, hobby, study
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pomodoro_sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long?,
    val type: String, // work, short_break, long_break
    val durationSec: Int,
    val startedAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = true
)

// Категории задач как в оригинале
object Categories {
    const val WORK = "work"
    const val REST = "rest"
    const val HOBBY = "hobby"
    const val STUDY = "study"

    val LABELS = mapOf(
        WORK to "Работа",
        REST to "Отдых",
        HOBBY to "Хобби",
        STUDY to "Учёба"
    )

    val ALL = listOf(WORK, REST, HOBBY, STUDY)
}

enum class SessionType {
    WORK, SHORT_BREAK, LONG_BREAK
}