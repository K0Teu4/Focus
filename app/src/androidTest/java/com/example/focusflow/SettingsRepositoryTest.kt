package com.example.focusflow

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.focusflow.data.repository.SettingsRepository
import com.example.focusflow.data.repository.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsRepositoryTest {
    private lateinit var repo: SettingsRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repo = SettingsRepository(context)
        runBlocking { context.dataStore.edit { it.clear() } }
    }

    @After
    fun tearDown() = runBlocking { context.dataStore.edit { it.clear() } }

    @Test
    fun defaults() = runBlocking {
        assertEquals(25, repo.workDuration.first())
        assertEquals(0, repo.workSeconds.first())
        assertEquals(5, repo.shortBreakDuration.first())
        assertEquals(15, repo.longBreakDuration.first())
        assertEquals(true, repo.soundEnabled.first())
        assertEquals(1.0f, repo.soundVolume.first(), 0.001f)
        assertEquals("bell", repo.soundType.first())
        assertEquals(false, repo.autoStart.first())
        assertEquals("dark", repo.theme.first())
        assertEquals(8, repo.dailyGoal.first())
        assertEquals(4, repo.sessionsUntilLongBreak.first())
        assertEquals(false, repo.isPremium.first())
        assertEquals(false, repo.onboardingCompleted.first())
    }

    @Test
    fun save_and_read_work() = runBlocking {
        repo.saveWorkDuration(0)
        repo.saveWorkSeconds(10)
        assertEquals(10, repo.getWorkDurationSeconds())
    }

    @Test
    fun work_seconds_calc() = runBlocking {
        repo.saveWorkDuration(1)
        repo.saveWorkSeconds(30)
        assertEquals(90, repo.getWorkDurationSeconds())
    }

    @Test
    fun breaks_calc() = runBlocking {
        repo.saveShortBreakDuration(2)
        repo.saveShortBreakSeconds(5)
        repo.saveLongBreakDuration(10)
        repo.saveLongBreakSeconds(0)
        assertEquals(125, repo.getShortBreakDurationSeconds())
        assertEquals(600, repo.getLongBreakDurationSeconds())
    }

    @Test
    fun premium_persists() = runBlocking {
        repo.saveIsPremium(true)
        repo.savePremiumCode("TEST-CODE")
        assertEquals(true, repo.isPremium.first())
    }

    @Test
    fun theme_save() = runBlocking {
        repo.saveTheme("ocean")
        assertEquals("ocean", repo.theme.first())
    }

    @Test
    fun ambient_save() = runBlocking {
        repo.saveAmbientType("pink")
        repo.saveAmbientVolume(0.3f)
        assertEquals("pink", repo.ambientType.first())
        assertEquals(0.3f, repo.ambientVolume.first(), 0.001f)
    }

    @Test
    fun onboarding_save() = runBlocking {
        repo.saveOnboardingCompleted(true)
        assertEquals(true, repo.onboardingCompleted.first())
    }
}