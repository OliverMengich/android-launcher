package com.example.android_launcher.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event (
    @PrimaryKey(autoGenerate = true) val id: Int=0,
    val title: String,
    val location: String?=null,
    val description: String,
    val notesPath: String? = "",
    val startDate: Long,
    val endDate: Long,
    val notesUrl: String,
    val eventCategory: EventCategory,
    val priority: Priority = Priority.HIGHEST,
    val dates: List<String>,

    val isPassed: Boolean? = false,
    val recurringType: EventRecurringType?= EventRecurringType.NONE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long= System.currentTimeMillis()
)
enum class EventCategory{
    EVENT,
    TASK,
    WORKING_LOCATION,
    OUT_OF_OFFICE
}

enum class EventRecurringType{
    NONE,
    DAILY,
    ONE_DAY_WEEKLY,
    WEEK_DAYS,
    SPECIFIC_DAYS_WEEKLY
}

enum class Priority{
    HIGHEST,
    HIGH,
    MEDIUM,
    LOW
}