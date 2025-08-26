package com.example.android_launcher.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.android_launcher.domain.models.Event
import com.example.android_launcher.domain.models.EventRecurringType

@Dao
interface CalendarDao {
    @Query(value = "SELECT * FROM events;")
    fun getAllEvents(): List<Event>

    @Query(value = "SELECT * FROM events;")
    fun getDayEvents(): List<Event>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Query(value="SELECT * FROM events WHERE recurringType=:recurringType;")
    fun getDailyRecurringEvents(recurringType: EventRecurringType = EventRecurringType.DAILY): List<Event>

    @Query(value="""
        SELECT * FROM events
        WHERE recurringType= :dailyRecurringType
        OR (startDate >= :startDate AND endDate <= :endDate)
        OR (recurringType=:specificDaysRecurring AND dates LIKE '%[' || :startDate || ']%')
        OR( recurringType=:weekDaysRecurring AND :isWeekDay=1)
        ORDER BY startDate ASC
    """)
    fun getTodayEvents(
        startDate: Long,
        endDate: Long,
        dailyRecurringType: EventRecurringType? = EventRecurringType.DAILY,
        weekDaysRecurring: EventRecurringType? = EventRecurringType.WEEK_DAYS,
        specificDaysRecurring: EventRecurringType? = EventRecurringType.WEEK_DAYS,
        isWeekDay: Boolean,
    ): List<Event>

    @Query(value="DELETE FROM events WHERE id=:id")
    fun deleteEvent(id: Int)

}