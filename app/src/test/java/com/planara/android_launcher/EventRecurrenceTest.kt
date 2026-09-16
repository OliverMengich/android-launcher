package com.planara.android_launcher

import com.planara.android_launcher.domain.models.EventRecurringType
import com.planara.android_launcher.domain.models.toRRule
import com.planara.android_launcher.domain.models.toRRuleDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.TimeZone

class EventRecurrenceTest {

    private val zone = ZoneId.of("Africa/Nairobi")
    private val tz = TimeZone.getTimeZone(zone)
    private fun millisOf(date: String) =
        LocalDate.parse(date).atStartOfDay(zone).toInstant().toEpochMilli()

    @Test
    fun weeklyRepeatOnSelectedDays() {
        val rule = EventRecurringType.SPECIFIC_DAYS_WEEKLY.toRRule(
            selectedDays = listOf(1, 3, 5),
            untilMillis = null,
            timeZone = tz
        )
        assertEquals("FREQ=WEEKLY;BYDAY=MO,WE,FR", rule)
    }

    @Test
    fun weeklyRepeatWithEndDateIncludesLastDay() {
        val rule = EventRecurringType.SPECIFIC_DAYS_WEEKLY.toRRule(
            selectedDays = listOf(2),
            untilMillis = millisOf("2026-11-30"),
            timeZone = tz
        )
        // UNTIL is the start of the day *after* the last repeat day (local time, here UTC+3),
        // so the last repeat day is included.
        assertEquals("FREQ=WEEKLY;BYDAY=TU;UNTIL=20261130T210000Z", rule)
    }

    @Test
    fun weekDaysRepeat() {
        val rule = EventRecurringType.WEEK_DAYS.toRRule(listOf(), null, timeZone = tz)
        assertEquals("FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR", rule)
    }

    @Test
    fun dailyRepeatEndsAtEndOfLastLocalDay() {
        val rule = EventRecurringType.DAILY.toRRule(
            selectedDays = emptyList(),
            untilMillis = millisOf("2026-11-30"),
            timeZone = tz
        )
        assertEquals("FREQ=DAILY;UNTIL=20261130T210000Z", rule)
    }

    @Test
    fun noRepeatAndSelectedDatesProduceNoRule() {
        assertNull(EventRecurringType.NONE.toRRule(emptyList(), null, timeZone = tz))
        assertNull(EventRecurringType.SELECTED_DATES.toRRule(emptyList(), null, timeZone = tz))
    }

    @Test
    fun isoDayNumbersMapToTokens() {
        assertEquals(
            listOf("MO", "TU", "WE", "TH", "FR", "SA", "SU"),
            (1..7).map { it.toRRuleDay() }
        )
    }
}
