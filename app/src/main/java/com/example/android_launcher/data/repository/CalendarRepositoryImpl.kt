package com.example.android_launcher.data.repository

import com.example.android_launcher.data.local.CalendarDao
import com.example.android_launcher.domain.models.Event
import com.example.android_launcher.domain.repository.CalendarRepository
import java.time.LocalDate
import java.util.Calendar

class CalendarRepositoryImpl(private val calendarDao: CalendarDao): CalendarRepository {
    override suspend fun getTodayEvents(startDay: Long): List<Event> {
        val calendar = Calendar.getInstance()
        val startOfDay = calendar.apply {
            timeInMillis = startDay
            set(Calendar.HOUR_OF_DAY,0)
            set(Calendar.MINUTE,0)
            set(Calendar.SECOND,0)
            set(Calendar.SECOND,0)
            set(Calendar.MILLISECOND,0)
        }.timeInMillis

        val endOfDay = calendar.apply {
            timeInMillis = startDay
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        val today = LocalDate.now()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val isWeekDay = dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY

        return calendarDao.getTodayEvents(startDate = startOfDay, endDate = endOfDay, isWeekDay = isWeekDay,)
    }

    override suspend fun deleteEvent(id: Int) {
        calendarDao.deleteEvent(id)
    }

    override suspend fun insertEvent(event: Event) {
        calendarDao.insertEvent(event)
    }

}