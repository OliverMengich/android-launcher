package com.example.android_launcher.domain.repository

import com.example.android_launcher.domain.models.Event
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface CalendarRepository {
    fun getTodayEvents(startDay: LocalDate): Flow<List<Event>>
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(id: Int)
    suspend fun insertEvent(event: Event)
}