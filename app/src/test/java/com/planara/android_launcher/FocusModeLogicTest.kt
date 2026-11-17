package com.planara.android_launcher

import com.planara.android_launcher.domain.models.FocusMode
import com.planara.android_launcher.domain.models.FocusType
import com.planara.android_launcher.domain.models.UsageTime
import com.planara.android_launcher.utils.isActive
import com.planara.android_launcher.utils.isExpired
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/**
 * Pure JVM checks for the focus-session scheduling rules. Run with:
 * `sh gradlew :app:testDebugUnitTest --tests "*FocusModeLogicTest"`
 */
class FocusModeLogicTest {

    private fun dt(s: String) = LocalDateTime.parse(s)

    private val now = dt("2026-11-12T10:30:00")

    // A long "focus until the end date" session.
    private val longSession = FocusMode(
        id = "long",
        endDate = "2026-11-20T23:59:00",
        focusType = FocusType.NORMAL
    )

    // A short "focus today only, 10am - 1pm" session.
    private val shortSession = FocusMode(
        id = "short",
        startDate = "2026-11-12T10:00:00",
        endDate = "2026-11-12T13:00:00",
        focusType = FocusType.FOCUS_BETWEEN,
        usageTimes = listOf(UsageTime("10:00", "13:00"))
    )

    @Test
    fun bothSessionsCanBeActiveAtTheSameTime() {
        val sessions = listOf(longSession, shortSession)
        assertTrue("long session should be active", longSession.isActive(now))
        assertTrue("short session should be in its window", shortSession.isActive(now))
        assertTrue("at least one of several sessions is active", sessions.any { it.isActive(now) })
    }

    @Test
    fun shortSessionIsNotActiveOutsideItsDateEvenIfTimeMatches() {
        // Same time of day, but a different day: the "today only" session must not fire.
        val tomorrowSameTime = dt("2026-11-13T10:30:00")
        assertFalse(shortSession.isActive(tomorrowSameTime))
        // But the long session still covers it.
        assertTrue(longSession.isActive(tomorrowSameTime))
    }

    @Test
    fun shortSessionIsNotActiveOutsideItsTimeWindow() {
        val before = dt("2026-11-12T09:00:00")
        val after = dt("2026-11-12T14:00:00")
        assertFalse(shortSession.isActive(before))
        assertFalse(shortSession.isActive(after))
    }

    @Test
    fun expiredSessionsArePruned() {
        val passed = dt("2026-11-21T00:10:00")
        assertTrue(longSession.isExpired(passed))
        assertTrue(shortSession.isExpired(passed))
        assertFalse(longSession.isActive(passed))
    }

    @Test
    fun noFocusBetweenInvertsTheWindow() {
        val lunch = FocusMode(
            id = "lunch",
            endDate = "2026-11-20T23:59:00",
            focusType = FocusType.NO_FOCUS_BETWEEN,
            usageTimes = listOf(UsageTime("12:00", "13:00"))
        )
        assertTrue("focus everywhere except lunch", lunch.isActive(dt("2026-11-12T11:00:00")))
        assertFalse("no focus during lunch", lunch.isActive(dt("2026-11-12T12:30:00")))
    }
}
