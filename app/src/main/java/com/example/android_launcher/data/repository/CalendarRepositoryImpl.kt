package com.example.android_launcher.data.repository

import android.util.Log
import com.example.android_launcher.data.local.CalendarDao
import com.example.android_launcher.domain.models.Event
import com.example.android_launcher.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate

class CalendarRepositoryImpl(private val calendarDao: CalendarDao): CalendarRepository {
    override fun getTodayEvents(startDay: LocalDate): Flow<List<Event>> {
        val dateString = startDay.toString()

        val isWeekDay = startDay.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        Log.d("CalendarRepositoryImpl", "getTodayEvents: $dateString")
        Log.d("CalendarRepositoryImpl", "getTodayEvents: ${startDay.dayOfWeek}. isWeekDay: $isWeekDay")
        return calendarDao.getTodayEvents(date = dateString, dayName = startDay.dayOfWeek.toString(), isWeekDay = isWeekDay)
    }

    override suspend fun updateEvent(event: Event) {
        calendarDao.updateEvent(event)
    }

    override suspend fun deleteEvent(id: Int) {
        calendarDao.deleteEvent(id)
    }

    override suspend fun insertEvent(event: Event) {
        calendarDao.insertEvent(event)
    }
}