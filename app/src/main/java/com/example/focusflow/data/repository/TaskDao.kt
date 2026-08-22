package com.example.focusflow.data.repository

import androidx.room.*
import com.example.focusflow.data.model.TaskEntity
import com.example.focusflow.data.model.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isDone = 0 ORDER BY createdAt DESC")
    fun getActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE category = :category AND isDone = 0 ORDER BY createdAt DESC")
    fun getTasksByCategory(category: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("UPDATE tasks SET isDone = :isDone WHERE id = :taskId")
    suspend fun setTaskDone(taskId: Long, isDone: Boolean)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startedAt DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int = 50): List<SessionEntity>

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE type = 'work' AND isCompleted = 1")
    suspend fun getTotalCompletedSessions(): Int

    @Query("SELECT SUM(durationSec) FROM pomodoro_sessions WHERE type = 'work' AND isCompleted = 1")
    suspend fun getTotalWorkSeconds(): Int
}