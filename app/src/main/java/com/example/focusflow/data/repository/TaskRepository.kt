package com.example.focusflow.data.repository

import com.example.focusflow.data.model.TaskEntity
import com.example.focusflow.data.model.SessionEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val activeTasks: Flow<List<TaskEntity>> = taskDao.getActiveTasks()

    fun getTasksByCategory(category: String): Flow<List<TaskEntity>> {
        return taskDao.getTasksByCategory(category)
    }

    suspend fun insert(task: TaskEntity): Long {
        return taskDao.insertTask(task)
    }

    suspend fun update(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun delete(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun getTaskById(id: Long): TaskEntity? {
        return taskDao.getTaskById(id)
    }

    suspend fun setTaskDone(taskId: Long, isDone: Boolean) {
        taskDao.setTaskDone(taskId, isDone)
    }
}

class SessionRepository(private val sessionDao: SessionDao) {
    val allSessions: Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    suspend fun saveSession(
        taskId: Long?,
        type: String,
        durationSec: Int,
        isCompleted: Boolean = true
    ) {
        sessionDao.insertSession(
            SessionEntity(
                taskId = taskId,
                type = type,
                durationSec = durationSec,
                isCompleted = isCompleted
            )
        )
    }

    suspend fun getRecentSessions(limit: Int = 50): List<SessionEntity> {
        return sessionDao.getRecentSessions(limit)
    }

    suspend fun getTotalCompletedSessions(): Int {
        return sessionDao.getTotalCompletedSessions()
    }

    suspend fun getTotalWorkSeconds(): Int {
        return sessionDao.getTotalWorkSeconds()
    }
}