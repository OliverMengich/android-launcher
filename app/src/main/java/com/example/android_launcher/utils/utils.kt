package com.example.android_launcher.utils

import android.content.Context
import android.content.pm.PackageManager
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

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
fun formatTimeFromMillis(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}