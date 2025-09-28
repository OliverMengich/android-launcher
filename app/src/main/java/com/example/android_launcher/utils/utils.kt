package com.example.android_launcher.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.Color
import com.example.android_launcher.domain.models.EventRecurringType
import com.example.android_launcher.domain.models.Priority
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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
fun LocalTime.formatTimeToRequiredFormat(timeFormat: String? = "24hr"): String {
    return when (timeFormat) {
        "24hr" -> String.format(Locale.getDefault(),"%02d:%02d hrs", hour, minute)
        "12hr" -> {
            val hour12 = when {
                hour == 0 -> 12        // midnight
                hour > 12 -> hour - 12 // afternoon
                else -> hour           // 1–12 AM
            }
            val amPm = if (hour < 12) "am" else "pm"
            String.format(Locale.getDefault(), "%02d:%02d%s", hour12, minute, amPm)
        }
        else -> ""
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