package com.example.android_launcher.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.Color
import com.example.android_launcher.domain.models.AppFonts
import com.example.android_launcher.domain.models.EventRecurringType
import com.example.android_launcher.domain.models.Priority
import com.example.android_launcher.presentation.screens.home.home.getDaySuffix
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val givenDateTime = LocalDateTime.parse(isDateTime,formatter)
    val now = LocalDateTime.now()
    return givenDateTime.isBefore(now)
}
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
        pattern.contains("hh") -> pattern.replaceFirst("hh", String.format(locale, "%02d", displayHour))
        pattern.contains("h") -> pattern.replaceFirst("h", String.format(locale, "%d", displayHour))
        pattern.contains("HH") -> pattern.replaceFirst("HH", String.format(locale, "%02d", displayHour))
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
    EventRecurringType.WEEK_DAYS to  "Week days",
    EventRecurringType.SPECIFIC_DAYS_WEEKLY to "The selected days weekly"
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