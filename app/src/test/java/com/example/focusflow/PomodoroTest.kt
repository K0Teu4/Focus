package com.example.focusflow

import com.example.focusflow.data.model.SessionType
import com.example.focusflow.utils.Pomodoro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PomodoroTest {
    @Test fun `formatTime zero`() = assertEquals("00:00", Pomodoro.formatTime(0))
    @Test fun `formatTime 61`() = assertEquals("01:01", Pomodoro.formatTime(61))
    @Test fun `formatTime 599`() = assertEquals("09:59", Pomodoro.formatTime(599))
    @Test fun `formatTime 3599`() = assertEquals("59:59", Pomodoro.formatTime(3599))
    @Test fun `formatTime 1500`() = assertEquals("25:00", Pomodoro.formatTime(1500))

    @Test fun `duration 0`() = assertEquals("0 мин", Pomodoro.formatDuration(0))
    @Test fun `duration 60`() = assertEquals("1 мин", Pomodoro.formatDuration(60))
    @Test fun `duration 61`() = assertEquals("1 мин 1 сек", Pomodoro.formatDuration(61))
    @Test fun `duration 73`() = assertEquals("1 мин 13 сек", Pomodoro.formatDuration(73))
    @Test fun `duration 120`() = assertEquals("2 мин", Pomodoro.formatDuration(120))

    @Test fun `minutes 1_0`() = assertEquals("1", Pomodoro.formatMinutes(1.0f))
    @Test fun `minutes 1_2`() = assertEquals("1.2", Pomodoro.formatMinutes(1.2f))
    @Test fun `minutes 0_17 rounds to 0_2`() = assertEquals("0.2", Pomodoro.formatMinutes(0.17f))
    @Test fun `minutes 0`() = assertEquals("0", Pomodoro.formatMinutes(0f))
    @Test fun `minutes 10_0`() = assertEquals("10", Pomodoro.formatMinutes(10f))

    @Test fun `delta null when both zero`() = assertNull(Pomodoro.deltaPct(0f, 0f))
    @Test fun `delta 100 when prev zero`() = assertEquals(100, Pomodoro.deltaPct(5f, 0f))
    @Test fun `delta plus 50`() = assertEquals(50, Pomodoro.deltaPct(150f, 100f))
    @Test fun `delta minus 50`() = assertEquals(-50, Pomodoro.deltaPct(50f, 100f))
    @Test fun `delta zero`() = assertEquals(0, Pomodoro.deltaPct(100f, 100f))

    @Test fun `after break always work`() =
        assertEquals(SessionType.WORK, Pomodoro.nextSessionType(false, 0, 4))
    @Test fun `3rd pomodoro then short break`() =
        assertEquals(SessionType.SHORT_BREAK, Pomodoro.nextSessionType(true, 2, 4))
    @Test fun `4th pomodoro then long break`() =
        assertEquals(SessionType.LONG_BREAK, Pomodoro.nextSessionType(true, 3, 4))
    @Test fun `8th pomodoro then long break`() =
        assertEquals(SessionType.LONG_BREAK, Pomodoro.nextSessionType(true, 7, 4))
    @Test fun `custom until long 2`() =
        assertEquals(SessionType.LONG_BREAK, Pomodoro.nextSessionType(true, 1, 2))
}