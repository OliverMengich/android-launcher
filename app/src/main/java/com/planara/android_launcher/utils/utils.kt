package com.planara.android_launcher.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.planara.android_launcher.domain.models.AppFonts
import com.planara.android_launcher.domain.models.EventRecurringType
import com.planara.android_launcher.domain.models.FocusMode
import com.planara.android_launcher.domain.models.FocusType
import com.planara.android_launcher.domain.models.Priority
import com.planara.android_launcher.domain.models.UsageTime
import com.planara.android_launcher.presentation.screens.home.home.getDaySuffix
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.String

fun formatTime(milliSecs: Long?): String {
    if (milliSecs == null || milliSecs <= 0) return ""

    val totalSeconds = milliSecs / 1000
    val seconds = totalSeconds % 60
    val totalMinutes = totalSeconds / 60
    val minutes = totalMinutes % 60
    val hours = totalMinutes / 60

    return when {
        hours > 0 -> "$hours hrs $minutes mins $seconds secs"
        minutes > 0 -> "$minutes mins $seconds secs"
        else -> "$seconds secs"
    }
}
fun longToMilliSeconds(timestamp: Long): LocalTime{
    val localTime: LocalTime = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    return localTime
}
fun localTimeToMillisToday(time: LocalTime): Long {
    val today = LocalDate.now()
    val dateTime = LocalDateTime.of(today, time)
    val zone = ZoneId.systemDefault()
    return dateTime.atZone(zone).toInstant().toEpochMilli()
}
fun isNowBetween(start: String, end: String): Boolean {
    val now = LocalTime.now()
    val startTime = LocalTime.parse(start)
    val endTime = LocalTime.parse(end)
    println("now: $now. startTime: $startTime. End time: $endTime")
    println("now: ${now >= startTime || now <= endTime}")
    println("now in : ${now in startTime..endTime}")

    return when {
        startTime == endTime -> true
        startTime < endTime ->
            now in startTime..endTime
        else ->
            now >= startTime || now <= endTime
    }
}
fun formatIsoTimeToFriendly(input: String?): String {
    if(input==null){
        return ""
    }
    val isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val dateTime = LocalDateTime.parse(input, isoFormatter)

    val today = LocalDate.now()
    val inputDate= dateTime.toLocalDate()
    return if (today==inputDate){
        val timeFormatter = DateTimeFormatter.ofPattern("h:mma", Locale.getDefault())
        dateTime.format(timeFormatter).lowercase()
    }else {
        val dayOfMonth = dateTime.dayOfMonth
        val daySuffix = getDayOfMonthSuffix(dayOfMonth)
        val outputFormatter = DateTimeFormatter.ofPattern("d'$daySuffix' MMMM h:mma", Locale.getDefault())
        dateTime.format(outputFormatter).lowercase().replace("am", "am").replace("pm", "pm")
    }
}
fun getDayOfMonthSuffix(n: Int): String {
    if (n in 11..13) return "th"
    return when (n % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}
fun isDatePassed(isDateTime: String?): Boolean{
    if (isDateTime.isNullOrBlank()) return false
    return try {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val givenDateTime = LocalDateTime.parse(isDateTime,formatter)
        val now = LocalDateTime.now()
        givenDateTime.isBefore(now)
    } catch (e: Exception) {
        false
    }
}

/**
 * Whether a focus window is in effect right now. When [sessionStart]/[sessionEnd] are set the
 * window is also restricted to that date range (e.g. "today 10am - 1pm" instead of every day).
 */
fun isWindowNow(
    window: UsageTime,
    sessionStart: String? = null,
    sessionEnd: String? = null,
    now: LocalDateTime = LocalDateTime.now()
): Boolean {
    if (window.startTime.isBlank() || window.endTime.isBlank()) return false
    if (sessionStart != null && safeParseLocalDateTime(sessionStart)?.isAfter(now) == true) return false
    if (sessionEnd != null && safeParseLocalDateTime(sessionEnd)?.isBefore(now) == true) return false
    // Compare against `now` (not the wall clock) so callers can evaluate arbitrary instants.
    return isTimeBetween(now.toLocalTime(), window.startTime, window.endTime)
}

/** [isNowBetween] without depending on the wall clock, so it can be evaluated for any instant. */
private fun isTimeBetween(now: LocalTime, start: String, end: String): Boolean {
    val startTime = runCatching { LocalTime.parse(start) }.getOrNull() ?: return false
    val endTime = runCatching { LocalTime.parse(end) }.getOrNull() ?: return false
    return when {
        startTime == endTime -> true
        startTime < endTime -> now in startTime..endTime
        else -> now >= startTime || now <= endTime
    }
}

/**
 * Whether a focus block should be enforced right now for the given scheduling type.
 *
 * - NORMAL:            always true.
 * - FOCUS_BETWEEN:     true only inside one of the windows.
 * - NO_FOCUS_BETWEEN:  true everywhere except inside one of the windows.
 */
fun isFocusBlockActive(
    focusType: FocusType,
    usageTimes: List<UsageTime>,
    sessionStart: String? = null,
    sessionEnd: String? = null,
    now: LocalDateTime = LocalDateTime.now()
): Boolean {
    return when (focusType) {
        FocusType.NORMAL -> true
        FocusType.FOCUS_BETWEEN -> usageTimes.any { isWindowNow(it, sessionStart, sessionEnd, now) }
        FocusType.NO_FOCUS_BETWEEN -> usageTimes.none { isWindowNow(it, sessionStart, sessionEnd, now) }
    }
}

private fun safeParseLocalDateTime(value: String?): LocalDateTime? {
    if (value.isNullOrBlank()) return null
    return runCatching { LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }.getOrNull()
}

/** The effective windows of a session, converting the legacy single range if needed. */
fun FocusMode.focusWindows(): List<UsageTime> =
    if (usageTimes.isEmpty() && startTime.isNotBlank() && endTime.isNotBlank()) {
        listOf(UsageTime(startTime = startTime, endTime = endTime))
    } else {
        usageTimes
    }

private fun FocusMode.effectiveFocusType(): FocusType =
    if (usageTimes.isEmpty() && startTime.isNotBlank() && endTime.isNotBlank()) FocusType.FOCUS_BETWEEN else focusType

/** True when the session's overall start/end date range is in effect (or open ended). */
fun FocusMode.isWithinSessionDates(now: LocalDateTime = LocalDateTime.now()): Boolean {
    safeParseLocalDateTime(startDate)?.let { if (it.isAfter(now)) return false }
    safeParseLocalDateTime(endDate)?.let { if (it.isBefore(now)) return false }
    return true
}

/** True when the session is still relevant, i.e. its end date has not passed. */
fun FocusMode.isExpired(now: LocalDateTime = LocalDateTime.now()): Boolean =
    safeParseLocalDateTime(endDate)?.isBefore(now) == true

/** True when the session has not expired and is scheduled to be enforced right now. */
fun FocusMode.isActive(now: LocalDateTime = LocalDateTime.now()): Boolean {
    if (isExpired(now)) return false
    return isWithinSessionDates(now) && isFocusBlockActive(effectiveFocusType(), focusWindows(), startDate, endDate, now)
}

/** True when the session has not expired yet (so it is either active or will become active). */
fun FocusMode.isScheduled(): Boolean = !isExpired()

/** Human readable list of the scheduled windows for the given focus mode. */
fun FocusMode.windowsLabel(): String = focusWindows()
    .filter { it.startTime.isNotBlank() && it.endTime.isNotBlank() }
    .joinToStringWithAnd { (start, end) ->
        val st = runCatching { LocalTime.parse(start).formatTimeToRequiredFormat("hh:mm a") }.getOrDefault(start)
        val et = runCatching { LocalTime.parse(end).formatTimeToRequiredFormat("hh:mm a") }.getOrDefault(end)
        "$st - $et"
    }

/** Human readable one-line description of what a focus session does. */
fun FocusMode.describe(): String = buildString {
    when (effectiveFocusType()) {
        FocusType.NORMAL -> append("Focus all day")
        FocusType.FOCUS_BETWEEN -> append("Focus only during ${windowsLabel()}")
        FocusType.NO_FOCUS_BETWEEN -> append("Focus all day except ${windowsLabel()}")
    }
    when {
        startDate.isNotBlank() && endDate.isNotBlank() -> append(", from ${formatIsoTimeToFriendly(startDate)} to ${formatIsoTimeToFriendly(endDate)}")
        endDate.isNotBlank() -> append(", until ${formatIsoTimeToFriendly(endDate)}")
        startDate.isNotBlank() -> append(", from ${formatIsoTimeToFriendly(startDate)}")
    }
}

/** The most specific session currently in effect, if any. */
fun List<FocusMode>.mostRelevantActive(): FocusMode? =
    filter { it.isActive() }.maxByOrNull { it.endDate }

fun isAppInstalled(context: Context, packageName: String): Boolean {
    return try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}
fun LocalTime.formatTimeToRequiredFormat(pattern: String? = "HH:mm"): String {
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        return this.format(formatter)

//    return when (timeFormat) {
//        "24hr" -> String.format(Locale.getDefault(),"%02d:%02d hrs", hour, minute)
//        "12hr" -> {
//            val hour12 = when {
//                hour == 0 -> 12        // midnight
//                hour > 12 -> hour - 12 // afternoon
//                else -> hour           // 1–12 AM
//            }
//            val amPm = if (hour < 12) "am" else "pm"
//            String.format(Locale.getDefault(), "%02d:%02d%s", hour12, minute, amPm)
//        }
//        else -> ""
//    }
}
fun formatLocalDateToRequiredFormat(dateFormat: String? = "custom1",locale: Locale=Locale.getDefault(),calendar: Calendar = Calendar.getInstance()): String {
    return when (dateFormat) {
        "custom1","custom2"->{
            val basePattern = if(dateFormat=="custom1"){
                "EEEE, dd MMMM yyyy"
            }else{
                "EEEE, dd MMMM"
            }
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val daySfx = getDaySuffix(dayOfMonth)
            val patternWithSuffix = basePattern
                .replace("dd", "d'$daySfx'")
//                .replace("d", "d'$daySfx'")
            val finalFormatter = SimpleDateFormat(patternWithSuffix, locale)
            finalFormatter.format(calendar.time)
        }
        else -> {
            val formatter = SimpleDateFormat(dateFormat, locale)
            formatter.format(calendar.time)
        }
    }
}
fun formatGivenDateToRequiredFormat(
    givenDate: LocalDate,
    dateFormat: String? = "custom1",
    locale: Locale = Locale.getDefault()
): String {
    fun getDaySuffix(day: Int): String = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }

    return when (dateFormat) {
        "custom1", "custom2" -> {
            val dayOfMonth = givenDate.dayOfMonth
            val daySfx = getDaySuffix(dayOfMonth)
            val basePattern = if (dateFormat == "custom1") "EEEE, d'%s' MMMM yyyy" else "EEEE, d'%s' MMMM"
            val patternWithSuffix = basePattern.format(daySfx)
            val formatter = DateTimeFormatter.ofPattern(patternWithSuffix, locale)
            givenDate.format(formatter)
        }
        else -> {
            val formatter = DateTimeFormatter.ofPattern(dateFormat, locale)
            givenDate.format(formatter)
        }
    }
}
fun formatHourPattern(pattern: String, hour24: Int, locale: Locale = Locale.getDefault()): String {

    val hasAmPm = pattern.contains("a")

    val (displayHour, amPm) = if (hasAmPm) {
        val amPm = if (hour24 < 12) "am " else "pm "
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        hour12 to amPm
    } else {
        hour24 to ""
    }

    val hourFormatted = when {
        pattern.contains("HH") -> pattern.replaceFirst("HH", String.format(locale, "%02d", displayHour)).replace("'", "")
        pattern.contains("H") -> pattern.replaceFirst("H", String.format(locale, "%d", displayHour)).replace("'", "")
        pattern.contains("hh") -> pattern.replaceFirst("hh", String.format(locale, "%02d", displayHour))
        pattern.contains("h") -> pattern.replaceFirst("h", String.format(locale, "%d", displayHour))
        else -> pattern
    }
    return if (hasAmPm) {
        hourFormatted.replace("a", amPm)
    } else {
        hourFormatted
    }
}
val repeatOptions = mapOf<EventRecurringType, String>(
    EventRecurringType.NONE to "No repeat",
    EventRecurringType.SELECTED_DATES to "No repeat, only on selected dates",
    EventRecurringType.WEEK_DAYS to  "Week days",
    EventRecurringType.SPECIFIC_DAYS_WEEKLY to "The selected days weekly",
)
val notifyOptions = mapOf<Int, String>(
    5 to "5 minutes before",
    10 to "10 minutes before",
    15 to "15 minutes before",
    30 to "30 minutes before",
    60 to "1 hour before",
)
val priorityColors = mapOf<Priority, Color>(
    Priority.LOW to Color(0xff4EFF57),
    Priority.MEDIUM to Color(0xffFBC02D),
    Priority.HIGH to Color(0xffF57C00),
    Priority.HIGHEST to Color(0xffD32F2F)
)

val timeFormatOptions = mapOf(
    "HH:mm" to "07:45",
    "H:mm" to "7:45",
    "H:mm 'hrs'" to "7:45hrs",
    "HH:mm 'hrs'" to "07:45hrs",
    "HH:mm:ss" to "07:45:45",
    "H:mm:ss" to "7:45:45",
    "h:mm" to "7:45",
    "h:mm a" to "7:45 am",
    "hh:mm a" to "07:45 am",
    "h:mm:ss a" to "7:45:45 am",
    "hh:mm:ss a" to "07:45:45 am",
)

val dateFormatOptions = mapOf(
    "dd/MM/yyyy" to "10/01/1970",
    "EEEE, dd MMMM yyyy" to "Monday, 10 January 1970",
    "dd MMMM yyyy" to "10 January 1970",
    "yyyy-MM-dd" to "1970-01-10",
    "MM/dd/yyyy" to "01/10/1970",
    "EEE, MMM dd, yyyy" to "Mon, Jan 10, 1970",
    "custom1" to "Monday, 10th January 1970",
    "custom2" to "Monday, 10th January"
)
val appFonts = mapOf(
    AppFonts.Default to "Default",
    AppFonts.Inter to "Inter",
    AppFonts.SpaceGrotesk to "Space Grotesk",
    AppFonts.Montserrat to "Montserrat",
    AppFonts.Sarina to "Sarina",
    AppFonts.CrosiantMono to "Croissant Mono",
    AppFonts.DancingScript to "Dancing Script"
)
fun <T> List<T>.joinToStringWithAnd(transform: (T) -> String): String =
    when (size) {
        0 -> ""
        1 -> transform(first())
        else -> dropLast(1).joinToString(", ", transform = transform) +
                " and " + transform(last())
    }

/** Absolute time a running focus session ends, used by the countdown. */
fun FocusMode.currentWindowEnd(now: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
    if (!isActive(now)) return null
    val sessionEnd = safeParseLocalDateTime(endDate)
    // All-day / always-on sessions are bounded only by their end date.
    if (effectiveFocusType() == FocusType.NORMAL) return sessionEnd

    val windowEnd = focusWindows()
        .filter { isWindowNow(it, startDate, endDate, now) }
        .mapNotNull { window ->
            runCatching {
                // An end time earlier than the start means the window runs past midnight.
                val startsAfterEnd = LocalTime.parse(window.endTime) < LocalTime.parse(window.startTime)
                val day = if (startsAfterEnd) now.toLocalDate().plusDays(1) else now.toLocalDate()
                day.atTime(LocalTime.parse(window.endTime))
            }.getOrNull()
        }
        .filter { it.isAfter(now) }
        .minOrNull()

    // Never run past the session's own end date.
    return listOfNotNull(windowEnd, sessionEnd).minOrNull()
}

/** The focus window containing [now], used to show the current start/end hours. */
fun FocusMode.currentWindow(now: LocalDateTime = LocalDateTime.now()): UsageTime? =
    focusWindows().filter { isWindowNow(it, startDate, endDate, now) }.minByOrNull { it.startTime }

/** Formats a millis duration as `HH:mm:ss` for the countdown. */
fun formatCountdown(millis: Long): String {
    val totalSeconds = (millis.coerceAtLeast(0L)) / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}
