package com.example.android_launcher.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.android_launcher.data.local.ListTypeConverter
import com.example.android_launcher.data.local.LocalTimeConverter
import java.time.LocalTime

@Entity(tableName = "events")
@TypeConverters(ListTypeConverter::class, LocalTimeConverter::class)
data class Event (
    @PrimaryKey(autoGenerate = true) val id: Int =0,
    val title: String,
    val location: String? =null,
    val description: String,
    val notesPath: String? = "",
    val startTime: LocalTime,
    val endTime: LocalTime,
    val notesUrl: String,
    val endDate: String? = null,
    val eventCategory: EventCategory,
    val priority: Priority = Priority.LOW,
    val dates: List<String>,
    val notifyBeforeTime: Int,
    val recurringType: EventRecurringType? = EventRecurringType.NONE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
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

enum class Priority{
    HIGHEST,
    HIGH,
    MEDIUM,
    LOW
}