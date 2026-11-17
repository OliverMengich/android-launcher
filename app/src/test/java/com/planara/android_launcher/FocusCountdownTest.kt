package com.planara.android_launcher

import com.planara.android_launcher.domain.models.FocusMode
import com.planara.android_launcher.domain.models.FocusType
import com.planara.android_launcher.domain.models.UsageTime
import com.planara.android_launcher.utils.currentWindow
import com.planara.android_launcher.utils.currentWindowEnd
import com.planara.android_launcher.utils.formatCountdown
import com.planara.android_launcher.utils.isActive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class FocusCountdownTest {

    private fun dt(s: String) = LocalDateTime.parse(s)
    private val now = dt("2026-11-12T10:30:15")

    private val allDay = FocusMode(
        id = "all",
        endDate = "2026-11-20T23:59:00",
        focusType = FocusType.NORMAL
    )

    private val timed = FocusMode(
        id = "timed",
        startDate = "2026-11-12T10:00:00",
        endDate = "2026-11-12T13:00:00",
        focusType = FocusType.FOCUS_BETWEEN,
        usageTimes = listOf(UsageTime("10:00", "13:00"))
    )

    @Test
    fun windowEndIsUsedWhileInsideTheWindow() {
        assertEquals(dt("2026-11-12T13:00"), timed.currentWindowEnd(now))
    }

    @Test
    fun windowEndIsNullWhenSessionIsNotActive() {
        assertNull(timed.currentWindowEnd(dt("2026-11-12T14:00:00")))
    }

    @Test
    fun currentWindowReportsTheStartHour() {
        assertEquals("10:00", timed.currentWindow(now)?.startTime)
    }

    @Test
    fun allDaySessionEndsAtItsEndDate() {
        assertEquals(dt("2026-11-20T23:59"), allDay.currentWindowEnd(now))
        assertNull(allDay.currentWindow(now))
    }

    @Test
    fun countdownExtendsAcrossOvernightWindow() {
        // 22:00 - 02:00 session, evaluated at 23:30: should end at 02:00 the next day.
        val overnight = FocusMode(
            id = "night",
            endDate = "2026-11-13T02:00:00",
            focusType = FocusType.FOCUS_BETWEEN,
            usageTimes = listOf(UsageTime("22:00", "02:00"))
        )
        val at = dt("2026-11-12T23:30:00")
        assertNotNull(overnight.currentWindowEnd(at))
        // The window end is "today 02:00" which already passed, so the session end date is used.
        assertEquals(dt("2026-11-13T02:00"), overnight.currentWindowEnd(at))
        assertEquals(true, overnight.isActive(at))
    }

    @Test
    fun countdownFormatIsHoursMinutesSeconds() {
        assertEquals("02:05:09", formatCountdown((2 * 3600 + 5 * 60 + 9) * 1000L))
        assertEquals("00:00:00", formatCountdown(-5000))
    }
}
