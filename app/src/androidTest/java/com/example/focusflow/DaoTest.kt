package com.example.focusflow

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.focusflow.data.AppDatabase
import com.example.focusflow.data.model.SessionEntity
import com.example.focusflow.data.model.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DaoTest {
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After fun closeDb() = db.close()

    @Test fun insert_and_get_task() = runBlocking {
        val id = db.taskDao().insertTask(TaskEntity(title = "Тест"))
        val task = db.taskDao().getTaskById(id)
        assertEquals("Тест", task?.title); assertEquals("work", task?.category); assertEquals(false, task?.isDone)
    }
    @Test fun update_title() = runBlocking {
        val id = db.taskDao().insertTask(TaskEntity(title = "А"))
        db.taskDao().updateTask(TaskEntity(id = id, title = "Б", category = "study"))
        assertEquals("Б", db.taskDao().getTaskById(id)?.title)
        assertEquals("study", db.taskDao().getTaskById(id)?.category)
    }
    @Test fun set_done() = runBlocking {
        val id = db.taskDao().insertTask(TaskEntity(title = "Х"))
        db.taskDao().setTaskDone(id, true); assertEquals(true, db.taskDao().getTaskById(id)?.isDone)
        db.taskDao().setTaskDone(id, false); assertEquals(false, db.taskDao().getTaskById(id)?.isDone)
    }
    @Test fun delete_task() = runBlocking {
        val id = db.taskDao().insertTask(TaskEntity(title = "Del"))
        db.taskDao().deleteTask(TaskEntity(id = id, title = "Del"))
        assertNull(db.taskDao().getTaskById(id))
    }
    @Test fun all_tasks_flow_order() = runBlocking {
        db.taskDao().insertTask(TaskEntity(title = "1"))
        db.taskDao().insertTask(TaskEntity(title = "2"))
        assertEquals(2, db.taskDao().getAllTasks().first().size)
    }
    @Test fun active_tasks_filter() = runBlocking {
        val a = db.taskDao().insertTask(TaskEntity(title = "A"))
        db.taskDao().insertTask(TaskEntity(title = "B"))
        db.taskDao().setTaskDone(a, true)
        assertEquals(1, db.taskDao().getActiveTasks().first().size)
    }
    @Test fun category_filter() = runBlocking {
        db.taskDao().insertTask(TaskEntity(title = "W", category = "work"))
        db.taskDao().insertTask(TaskEntity(title = "H", category = "hobby"))
        assertEquals(1, db.taskDao().getTasksByCategory("hobby").first().size)
    }
    @Test fun insert_session() = runBlocking {
        db.sessionDao().insertSession(SessionEntity(taskId = null, type = "work", durationSec = 60))
        assertEquals(1, db.sessionDao().getRecentSessions(10).size)
    }
    @Test fun recent_order_desc() = runBlocking {
        db.sessionDao().insertSession(SessionEntity(taskId = null, type = "work", durationSec = 10, startedAt = 100))
        db.sessionDao().insertSession(SessionEntity(taskId = null, type = "work", durationSec = 20, startedAt = 200))
        val list = db.sessionDao().getRecentSessions(10)
        assertTrue(list.first().startedAt >= list.last().startedAt)
    }
    @Test fun completed_count() = runBlocking {
        db.sessionDao().insertSession(SessionEntity(taskId = null, type = "work", durationSec = 60, isCompleted = true))
        db.sessionDao().insertSession(SessionEntity(taskId = null, type = "work", durationSec = 30, isCompleted = false))
        db.sessionDao().insertSession(SessionEntity(taskId = null, type = "short_break", durationSec = 60, isCompleted = true))
        assertEquals(1, db.sessionDao().getTotalCompletedSessions())
    }
    @Test fun total_work_seconds() = runBlocking {
        db.sessionDao().insertSession(SessionEntity(taskId = null, type = "work", durationSec = 60, isCompleted = true))
        db.sessionDao().insertSession(SessionEntity(taskId = null, type = "work", durationSec = 30, isCompleted = true))
        assertEquals(90, db.sessionDao().getTotalWorkSeconds())
    }
}