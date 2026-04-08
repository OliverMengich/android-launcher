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
    val selectedRecurringType: EventRecurringType = EventRecurringType.SELECTED_DATES
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
fun Int.toRRuleDay(): String {
    return when (this) {
        Calendar.MONDAY -> "MO"
        Calendar.TUESDAY -> "TU"
        Calendar.WEDNESDAY -> "WE"
        Calendar.THURSDAY -> "TH"
        Calendar.FRIDAY -> "FR"
        Calendar.SATURDAY -> "SA"
        Calendar.SUNDAY -> "SU"
        else -> throw IllegalArgumentException("Invalid day")
    }
}
fun formatUtc(timeInMillis: Long): String {
    val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(timeInMillis))
}
enum class Priority{
    HIGHEST,
    HIGH,
    MEDIUM,
    LOW
}