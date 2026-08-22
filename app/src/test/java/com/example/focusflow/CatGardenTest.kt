package com.example.focusflow

import com.example.focusflow.utils.CatAchievement
import com.example.focusflow.utils.CatGarden
import com.example.focusflow.utils.GardenStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatGardenTest {

    private val s = { p: Int, st: Int, f: Int, t: Int -> GardenStats(p, st, f, t) }

    @Test
    fun `first cat unlocks at 1 pomodoro`() {
        assertTrue(CatGarden.achievements.first { it.id == "kitten" }.isUnlocked(s(1, 0, 0, 0)))
    }

    @Test
    fun `no unlocks at zero`() {
        assertEquals(0, CatGarden.unlockedCount(s(0, 0, 0, 0)))
    }

    @Test
    fun `unlocked count grows`() {
        assertEquals(3, CatGarden.unlockedCount(s(10, 0, 0, 0)))
    }

    @Test
    fun `streak and tasks and focus count`() {
        // 3 pomodoro-кота + fire3 + task1 + hour1
        assertEquals(6, CatGarden.unlockedCount(s(10, 3, 70, 1)))
    }

    @Test
    fun `next pomodoro target`() {
        val next = CatGarden.nextPomodoro(7)
        assertEquals("worker", next?.id)
    }

    @Test
    fun `nearest picks smallest remaining`() {
        val stats = s(9, 0, 0, 0) // worker(10) осталось 1 — ближе всех
        assertEquals("worker", CatGarden.nearest(stats)?.id)
    }

    @Test
    fun `next is null when all pomodoro cats unlocked`() {
        assertNull(CatGarden.nextPomodoro(1000))
    }

    @Test
    fun `focus minutes kind works`() {
        val hour5 = CatGarden.achievements.first { it.id == "hour5" }
        assertTrue(!hour5.isUnlocked(s(0, 0, 299, 0)))
        assertTrue(hour5.isUnlocked(s(0, 0, 300, 0)))
    }

    @Test
    fun `total achievements is 20`() {
        assertEquals(20, CatGarden.achievements.size)
    }
}