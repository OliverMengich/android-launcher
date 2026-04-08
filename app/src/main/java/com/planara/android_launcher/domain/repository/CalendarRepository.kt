package com.planara.android_launcher.domain.repository

import android.net.Uri
import com.planara.android_launcher.domain.models.DeviceCalendar
import com.planara.android_launcher.domain.models.Event
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface CalendarRepository {
    fun getTodayEvents(startDay: LocalDate): Flow<List<Event>>
    suspend fun updateEvent(event: Event): Uri?
    fun getDeviceCalendars(): List<DeviceCalendar>
    fun hasCalendarPermissions(): Boolean
    suspend fun deleteEvent(id: Long): Boolean
    suspend fun insertEvent(event: Event): Long?
}