package com.example.android_launcher.domain.repository

import com.example.android_launcher.domain.models.Event

interface CalendarRepository {
    suspend fun getTodayEvents(startDay: Long): List<Event>
    suspend fun deleteEvent(id: Int)
    suspend fun insertEvent(event: Event)
}