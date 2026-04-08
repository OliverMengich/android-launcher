package com.planara.android_launcher.data.repository

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.planara.android_launcher.dataStore
import com.planara.android_launcher.domain.models.DeviceCalendar
import com.planara.android_launcher.domain.models.Event
import com.planara.android_launcher.domain.models.toRRule
import com.planara.android_launcher.domain.repository.CalendarRepository
import com.planara.android_launcher.presentation.screens.home.home.calendar.getStartAndEndOfDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.TimeZone

class CalendarRepositoryImpl(private val context: Context): CalendarRepository {

    override fun getTodayEvents(startDay: LocalDate): Flow<List<Event>> = flow {

        val hasCalendarPermission = ContextCompat.checkSelfPermission(context,Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        if (!hasCalendarPermission){
            emit(emptyList())
            return@flow
        }

        val startEndDate = getStartAndEndOfDate(startDay)
        val events = mutableListOf<Event>()

        val uri = CalendarContract.Instances.CONTENT_URI
            .buildUpon()
            .apply {
                ContentUris.appendId(this, startEndDate.first)
                ContentUris.appendId(this, startEndDate.second)
            }
            .build()
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.AVAILABILITY,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.CUSTOM_APP_URI
        )

        context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${CalendarContract.Instances.BEGIN} ASC"
        )?.use { cursor ->

            val idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
            val titleIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val descIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.DESCRIPTION)
            val locationIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)
            val beginIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val endIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
            val availabilityIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.AVAILABILITY)
            val allDayIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
            val customUriIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.CUSTOM_APP_URI)

            while (cursor.moveToNext()) {

                val eventId = cursor.getLong(idIndex)

                // 🔥 Query reminders separately
                val minutesBefore = getReminderMinutes(context, eventId)

                val event = Event(
                    id = eventId,
                    title = cursor.getString(titleIndex) ?: "",
                    description = cursor.getString(descIndex),
                    location = cursor.getString(locationIndex),
                    start = cursor.getLong(beginIndex),
                    end = cursor.getLong(endIndex),
                    availability = cursor.getInt(availabilityIndex),
                    isAllDay = cursor.getInt(allDayIndex) == 1,
                    customUri = cursor.getString(customUriIndex),
                    minutesBefore = minutesBefore
                )

                events.add(event)
            }
        }
            emit(events)

        }.flowOn(Dispatchers.IO)

    fun observeCalendarChanges(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(
            Handler(Looper.getMainLooper())
        ) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }

        context.contentResolver.registerContentObserver(
            CalendarContract.Events.CONTENT_URI,
            true,
            observer
        )

        awaitClose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }
    private suspend fun queryEvents(
        startMillis: Long,
        endMillis: Long
    ): List<Event> = withContext(Dispatchers.IO) {

        if (!hasCalendarPermissions()) return@withContext emptyList()

        val events = mutableListOf<Event>()

        val uri = CalendarContract.Instances.CONTENT_URI
            .buildUpon()
            .apply {
                ContentUris.appendId(this, startMillis)
                ContentUris.appendId(this, endMillis)
            }
            .build()

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.AVAILABILITY,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.CUSTOM_APP_URI
        )

        context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${CalendarContract.Instances.BEGIN} ASC"
        )?.use { cursor ->

            val idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
            val titleIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val descIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.DESCRIPTION)
            val locationIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)
            val beginIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val endIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
            val availabilityIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.AVAILABILITY)
            val allDayIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
            val customUriIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.CUSTOM_APP_URI)

            while (cursor.moveToNext()) {

                val eventId = cursor.getLong(idIndex)

                events.add(
                    Event(
                        id = eventId,
                        title = cursor.getString(titleIndex) ?: "",
                        description = cursor.getString(descIndex),
                        location = cursor.getString(locationIndex),
                        start = cursor.getLong(beginIndex),
                        end = cursor.getLong(endIndex),
                        availability = cursor.getInt(availabilityIndex),
                        isAllDay = cursor.getInt(allDayIndex) == 1,
                        customUri = cursor.getString(customUriIndex),
                        minutesBefore = getReminderMinutes(eventId)
                    )
                )
            }
        }

        events
    }
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun observeEvents(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<Event>> {
        return observeCalendarChanges()
            .onStart { emit(Unit) } // emit initially
            .debounce(300) // prevent rapid re-queries
            .flatMapLatest {
                flow {
                    emit(queryEvents(startMillis, endMillis))
                }
            }
    }
    override fun hasCalendarPermissions(): Boolean {
        val readGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        val writeGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        return readGranted && writeGranted
    }

    override suspend fun updateEvent(event: Event) = withContext(Dispatchers.IO) {

        val uri = ContentUris.withAppendedId(
            CalendarContract.Events.CONTENT_URI,
            event.id
        )
        event.selectedDates.forEach { date ->
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, date.start)
                put(CalendarContract.Events.DTEND, date.end)
            }

            context.contentResolver.insert(
                CalendarContract.Events.CONTENT_URI,
                values
            )
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DESCRIPTION, event.description)
            put(CalendarContract.Events.EVENT_LOCATION, event.location)
            put(CalendarContract.Events.DTSTART, event.start)
            put(CalendarContract.Events.DTEND, event.end)
            put(CalendarContract.Events.AVAILABILITY, event.availability)
            put(CalendarContract.Events.ALL_DAY, if (event.isAllDay) 1 else 0)
            put(CalendarContract.Events.CUSTOM_APP_URI, event.customUri)
            val rrule = event.selectedRecurringType.toRRule()
            rrule?.let {
                put(CalendarContract.Events.RRULE, it)
            }
        }

        context.contentResolver.update(uri, values, null, null)

        // Replace reminder
        event.minutesBefore?.let {
            context.contentResolver.delete(
                CalendarContract.Reminders.CONTENT_URI,
                "${CalendarContract.Reminders.EVENT_ID} = ?",
                arrayOf(event.id.toString())
            )

            val reminderValues = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, event.id)
                put(CalendarContract.Reminders.MINUTES, it)
                put(CalendarContract.Reminders.METHOD,
                    CalendarContract.Reminders.METHOD_ALERT)
            }

            context.contentResolver.insert(
                CalendarContract.Reminders.CONTENT_URI,
                reminderValues
            )
        }
    }

    override suspend fun deleteEvent(id: Long): Boolean {
        val uri = ContentUris.withAppendedId(
            CalendarContract.Events.CONTENT_URI,
            id
        )
        val rows = context.contentResolver.delete(uri, null, null)
        return rows > 0
    }
    override suspend fun insertEvent(event: Event): Long? = withContext(Dispatchers.IO) {

        if (!hasCalendarPermissions()) return@withContext null

        val calendarId = context.dataStore.data.first().selectedDeviceCalendar

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, event.start)
            put(CalendarContract.Events.DTEND, event.end)
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DESCRIPTION, event.description)
            put(CalendarContract.Events.EVENT_LOCATION, event.location)
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.AVAILABILITY, event.availability)
            put(CalendarContract.Events.ALL_DAY, if (event.isAllDay) 1 else 0)
            put(CalendarContract.Events.CUSTOM_APP_URI, event.customUri)
        }

        val uri = context.contentResolver.insert(
            CalendarContract.Events.CONTENT_URI,
            values
        )

        val eventId = uri?.lastPathSegment?.toLong()

        // Insert reminder separately
        event.minutesBefore.let { minutes ->
            val reminderValues = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, eventId)
                put(CalendarContract.Reminders.MINUTES, minutes)
                put(CalendarContract.Reminders.METHOD,
                    CalendarContract.Reminders.METHOD_ALERT)
            }

            context.contentResolver.insert(
                CalendarContract.Reminders.CONTENT_URI,
                reminderValues
            )
        }

        eventId
    }
    private fun getReminderMinutes(eventId: Long): Int? {

        val projection = arrayOf(
            CalendarContract.Reminders.MINUTES
        )

        val selection = "${CalendarContract.Reminders.EVENT_ID} = ?"
        val args = arrayOf(eventId.toString())

        context.contentResolver.query(
            CalendarContract.Reminders.CONTENT_URI,
            projection,
            selection,
            args,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(
                    CalendarContract.Reminders.MINUTES
                )
                return cursor.getInt(index)
            }
        }

        return null
    }
    override fun getDeviceCalendars(): List<DeviceCalendar> {

        val calendars = mutableListOf<DeviceCalendar>()

        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.NAME,
            CalendarContract.Calendars.ACCOUNT_NAME
        )

        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->

            val idIndex = cursor.getColumnIndexOrThrow(
                CalendarContract.Calendars._ID
            )
            val nameIndex = cursor.getColumnIndexOrThrow(
                CalendarContract.Calendars.NAME
            )
            val accountIndex = cursor.getColumnIndexOrThrow(
                CalendarContract.Calendars.ACCOUNT_NAME
            )

            while (cursor.moveToNext()) {
                calendars.add(
                    DeviceCalendar(
                        id = cursor.getLong(idIndex),
                        name = cursor.getString(nameIndex)?:"",
                        accountName = cursor.getString(accountIndex)
                    )
                )
            }
        }

        return calendars
    }
    fun getReminderMinutes(context: Context, eventId: Long): Int? {
        val cursor = context.contentResolver.query(
            CalendarContract.Reminders.CONTENT_URI,
            arrayOf(CalendarContract.Reminders.MINUTES),
            "${CalendarContract.Reminders.EVENT_ID} = ?",
            arrayOf(eventId.toString()),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                return it.getInt(
                    it.getColumnIndexOrThrow(CalendarContract.Reminders.MINUTES)
                )
            }
        }

        return null
    }
}