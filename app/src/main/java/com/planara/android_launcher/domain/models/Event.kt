package com.planara.android_launcher.domain.models

import com.planara.android_launcher.domain.models.EventRecurringType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class Event(
    val id: Long=0L,
    val title: String,
    val description: String?,
    val location: String?,
    val start: Long,
    val end: Long,
    val availability: Int,
    val isAllDay: Boolean,
    val minutesBefore: Int?,
    val customUri: String?,
    val selectedDates: List<EventSelectedDate> = emptyList(),
    val selectedRecurringType: EventRecurringType = EventRecurringType.SELECTED_DATES,
    /** ISO day numbers (1 = Mon … 7 = Sun) for weekly repeats. */
    val repeatWeekDays: List<Int> = emptyList(),
    /** Last day the event repeats on, in millis. Null repeats forever (or not at all). */
    val repeatUntil: Long? = null
)
data class EventSelectedDate(
    val start: Long,
    val end: Long
)


enum class EventCategory{
    EVENT,
    TASK,
    MEETING,
    WORKING_LOCATION,
    OUT_OF_OFFICE,
    BIRTHDAY
}

enum class EventRecurringType{
    NONE,
    DAILY,
    SPECIFIC_DAYS_WEEKLY,
    WEEK_DAYS,
    SELECTED_DATES
}
fun EventRecurringType.toRRule(
    selectedDays: List<Int> = emptyList(),
    count: Int? = null,
    until: Long? = null
): String? {
    return when (this) {
        EventRecurringType.NONE -> null
        EventRecurringType.DAILY -> {
            buildString {
                append("FREQ=DAILY")
                count?.let { append(";COUNT=$it") }
                until?.let { append(";UNTIL=${formatUtc(it)}") }
            }
        }

        EventRecurringType.WEEK_DAYS -> {
            "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR"
        }

        EventRecurringType.SPECIFIC_DAYS_WEEKLY -> {
            if (selectedDays.isEmpty()) return null

            val days = selectedDays.joinToString(",") { it.toRRuleDay() }
            "FREQ=WEEKLY;BYDAY=$days"
        }

        EventRecurringType.SELECTED_DATES -> {
            // No RRULE → you create multiple events manually
            null
        }
    }
}
fun formatUtc(timeInMillis: Long): String {
    val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(timeInMillis))
}
/** ISO day number (1 = Monday … 7 = Sunday) to an RRULE day token. */
fun Int.toRRuleDay(): String = when (this) {
    1 -> "MO"
    2 -> "TU"
    3 -> "WE"
    4 -> "TH"
    5 -> "FR"
    6 -> "SA"
    7 -> "SU"
    else -> throw IllegalArgumentException("Invalid day: $this")
}
enum class Priority{
    HIGHEST,
    HIGH,
    MEDIUM,
    LOW
}
/**
 * Builds a full RFC 5545 RRULE for the new-event flow, including an optional end
 * (either count or an until date), like Google Calendar / Outlook repeat options.
 *
 * @param selectedDays ISO day numbers (1 = Monday … 7 = Sunday) used for weekly repeats.
 * @param untilMillis last day the event may repeat on; converted to an exclusive UTC boundary.
 */
fun EventRecurringType.toRRule(
    selectedDays: List<Int>,
    untilMillis: Long?,
    interval: Int = 1,
    timeZone: TimeZone = TimeZone.getDefault()
): String? = when (this) {
    EventRecurringType.NONE -> null
    EventRecurringType.DAILY -> buildDailyRule(interval, untilMillis, timeZone)
    EventRecurringType.WEEK_DAYS -> buildString {
        append("FREQ=WEEKLY")
        if (interval > 1) append(";INTERVAL=$interval")
        append(";BYDAY=MO,TU,WE,TH,FR")
        untilMillis?.let { append(";UNTIL=${endOfLocalDayUtc(it, timeZone)}") }
    }
    EventRecurringType.SPECIFIC_DAYS_WEEKLY -> {
        if (selectedDays.isEmpty()) return null
        buildString {
            append("FREQ=WEEKLY")
            if (interval > 1) append(";INTERVAL=$interval")
            append(";BYDAY=${selectedDays.joinToString(",") { it.toRRuleDay() }}")
            untilMillis?.let { append(";UNTIL=${endOfLocalDayUtc(it, timeZone)}") }
        }
    }
    EventRecurringType.SELECTED_DATES -> {
        // No RRULE: separate events are created for each selected date.
        null
    }
}

private fun buildDailyRule(interval: Int, untilMillis: Long?, timeZone: TimeZone): String = buildString {
    append("FREQ=DAILY")
    if (interval > 1) append(";INTERVAL=$interval")
    untilMillis?.let { append(";UNTIL=${endOfLocalDayUtc(it, timeZone)}") }
}

/**
 * Converts a millis value on the last repeat day into the exclusive UTC boundary in RRULE form
 * (start of the next day in [timeZone]), so that last day is actually included.
 */
private fun endOfLocalDayUtc(dayMillis: Long, timeZone: TimeZone): String {
    val calendar = Calendar.getInstance(timeZone).apply {
        timeInMillis = dayMillis
        add(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return formatUtc(calendar.timeInMillis)
}


