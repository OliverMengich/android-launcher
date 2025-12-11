package com.example.android_launcher.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.android_launcher.domain.models.Event
import com.example.android_launcher.domain.models.EventRecurringType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

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
        WHERE (
            recurringType= :dailyRecurringType 
            AND endDate IS NULL OR endDate >= :todayDate 
        )
        OR (recurringType=:selectedDatesRecurring AND dates LIKE '%[' || :date || ']%')
        OR(recurringType=:specificDaysWeeklyRecurring AND dates LIKE '%[' || :dayName || ']%')
        OR(
            recurringType=:weekDaysRecurring
            AND :isWeekDay=1 
            AND (endDate IS NULL OR endDate >= :todayDate)
        )
        ORDER BY startTime ASC
    """)
    fun getTodayEvents(
        date: String,
        dayName: String?="",
        todayDate: String?= LocalDate.now().toString(),
        dailyRecurringType: EventRecurringType? = EventRecurringType.DAILY,
        weekDaysRecurring: EventRecurringType? = EventRecurringType.WEEK_DAYS,
        specificDaysWeeklyRecurring: EventRecurringType?= EventRecurringType.SPECIFIC_DAYS_WEEKLY,
        selectedDatesRecurring: EventRecurringType? = EventRecurringType.SELECTED_DATES,
        isWeekDay: Boolean,
    ): Flow<List<Event>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateEvent(event: Event)


    @Query(value="DELETE FROM events WHERE id=:id")
    fun deleteEvent(id: Int)

}