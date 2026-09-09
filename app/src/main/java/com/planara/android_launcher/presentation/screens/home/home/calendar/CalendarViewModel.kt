package com.planara.android_launcher.presentation.screens.home.home.calendar

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planara.android_launcher.domain.models.DeviceCalendar
import com.planara.android_launcher.domain.models.Event
import com.planara.android_launcher.domain.models.EventCategory
import com.planara.android_launcher.domain.models.EventRecurringType
import com.planara.android_launcher.domain.models.Priority
import com.planara.android_launcher.domain.repository.CalendarRepository
import com.planara.android_launcher.utils.localTimeToMillisToday
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import kotlin.reflect.KProperty1

data class NewEventPageState(
    val eventName: String = "",
    val locationLink: String = "",
    val description: String = "",
    val notifyBeforeTime: Int = 30,
    // Dates the user picked. These are the event days; for repeat they are the first occurrence
    // (or every occurrence when repeating on specific week days).
    val selectedDates: Set<LocalDate> = emptySet<LocalDate>(),
    val startTime: LocalTime = LocalTime.now().plusMinutes(30).withSecond(0).withNano(0),
    val endTime: LocalTime = LocalTime.now().plusHours(1).withSecond(0).withNano(0),
    /** When true the times are hidden and the event spans whole days. */
    val isAllDay: Boolean = false,
    val eventCategory: EventCategory = EventCategory.EVENT,
    val selectedPriority: Priority = Priority.LOW,
    val selectedRecurringType: EventRecurringType = EventRecurringType.NONE,
    /** How the event repeats: every N days/weeks. */
    val repeatInterval: Int = 1,
    /** Optional end of the repetition. Null means it repeats forever. */
    val repeatUntil: LocalDate? = null,
    /** Calendar (account) selected for this event. 0 means use the saved default. */
    val selectedCalendarId: Long = 0L,
) {
    val sortedDates: List<LocalDate> get() = selectedDates.sorted()
    val firstDate: LocalDate? get() = sortedDates.firstOrNull()
    val lastDate: LocalDate? get() = sortedDates.lastOrNull()
}

sealed class NewEventPageEvent{
    data class UpdateEventPageValue<T>(val field: KProperty1<NewEventPageState,T>, val value: T): NewEventPageEvent()
    data object Idle: NewEventPageEvent()
    data object CreateEvent: NewEventPageEvent()
    data class ShowErrorMessage(val message: String): NewEventPageEvent()
    data object ShowSuccess: NewEventPageEvent()
}
fun getStartAndEndOfDate(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Pair<Long, Long> {
    val start = date
        .atStartOfDay(zone)
        .toInstant()
        .toEpochMilli()

    val end = date
        .plusDays(1)
        .atStartOfDay(zone)
        .toInstant()
        .toEpochMilli() - 1

    return Pair(start, end)
}
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(private val calendarRepository: CalendarRepository, private val context: Context): ViewModel() {
    private val _newEventPageState = MutableStateFlow<NewEventPageState>(NewEventPageState())
    val newEventPageState: StateFlow<NewEventPageState> = _newEventPageState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = NewEventPageState()
    )
    private val _dates = MutableStateFlow< List<List<LocalDate>>>(emptyList())
    val dates: StateFlow<List<List<LocalDate>>> = _dates.asStateFlow()
    private val startDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val hasCalendarPermission = calendarRepository.hasCalendarPermissions()
    private val _deviceCalendars = MutableStateFlow<List<DeviceCalendar>>(value = emptyList())
    val deviceCalendars = _deviceCalendars.asStateFlow()

    /** Calendars/accounts available for creating events, loaded from the device provider. */
    fun getWritableCalendars() {
        viewModelScope.launch(Dispatchers.IO) {
            _deviceCalendars.value = calendarRepository.getDeviceCalendars()
        }
    }



    private val _todayEvents = MutableStateFlow<List<Event>>(emptyList())
    val todayEvents = _todayEvents.asStateFlow()
    fun getTodayEvents(){
        viewModelScope.launch(context=Dispatchers.IO) {
            val tdEvs = calendarRepository.getTodayEvents(startDay = LocalDate.now()).first()
            _todayEvents.value = tdEvs
        }
    }
//    val todayEvents = calendarRepository
//        .getTodayEvents(startDay = LocalDate.now())
//        .stateIn(
//            viewModelScope,
//            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
//            initialValue = emptyList()
//        )
    val dateEvents = startDate
        .flatMapLatest { dy->
            calendarRepository.getTodayEvents(startDay = dy)
        }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = emptyList()
        )
    private val _newPageEvent: Channel<NewEventPageEvent> = Channel()
    val newPageEvent =_newPageEvent.receiveAsFlow()
    fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        ).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
    fun getDeviceCalendars(){
        viewModelScope.launch {
            val cals = calendarRepository.getDeviceCalendars()
            _deviceCalendars.value = cals
        }
    }
    fun onEvent(event: NewEventPageEvent){
        when(event){
            is NewEventPageEvent.UpdateEventPageValue<*> -> {
                _newEventPageState.update { currentState->
                    when(event.field){
                        NewEventPageState::eventName -> currentState.copy(eventName = event.value as String)
                        NewEventPageState::locationLink-> currentState.copy(locationLink = event.value as String)
                        NewEventPageState::description -> currentState.copy(description = event.value as String)
                        NewEventPageState::selectedDates -> currentState.copy(selectedDates = event.value as Set<LocalDate>)
                        NewEventPageState::selectedPriority -> currentState.copy(selectedPriority = event.value as Priority)
                        NewEventPageState::startTime -> {
                            currentState.copy(
                                startTime = event.value as LocalTime,
                                endTime = LocalTime.of(event.value.hour, event.value.minute).plusMinutes(30)
                            )
                        }
                        NewEventPageState::endTime -> currentState.copy(endTime = event.value as LocalTime)
                        NewEventPageState::notifyBeforeTime -> currentState.copy(notifyBeforeTime = event.value as Int)
                        NewEventPageState::eventCategory -> currentState.copy(eventCategory =  event.value as EventCategory)
                        NewEventPageState::selectedRecurringType -> currentState.copy(
                            selectedRecurringType = event.value as EventRecurringType,
                            // Clearing the repeat should not keep stale repeat settings around.
                            repeatUntil = if (event.value == EventRecurringType.NONE) null else currentState.repeatUntil
                        )
                        NewEventPageState::isAllDay -> currentState.copy(isAllDay = event.value as Boolean)
                        NewEventPageState::repeatInterval -> currentState.copy(repeatInterval = (event.value as Int).coerceAtLeast(1))
                        NewEventPageState::repeatUntil -> currentState.copy(repeatUntil = event.value as LocalDate?)
                        NewEventPageState::selectedCalendarId -> currentState.copy(selectedCalendarId = event.value as Long)
                        else -> currentState
                    }
                }
            }
            is NewEventPageEvent.CreateEvent -> {
                val state = newEventPageState.value
                val validationError = validate(state)
                if (validationError != null) {
                    viewModelScope.launch { _newPageEvent.send(NewEventPageEvent.ShowErrorMessage(validationError)) }
                    return
                }
                viewModelScope.launch {
                    newEvent(state = state)
                }
            }
            else -> {}
        }
    }
//    fun getTodayEvents(){
//        viewModelScope.launch(context=Dispatchers.IO) {
//            val tdEvs = calendarRepository.getTodayEvents(startDay = LocalDate.now())
//            _todayEvents.value = tdEvs
//        }
//    }
    fun getDateEvents(startDay: LocalDate){
        startDate.value = startDay
//        viewModelScope.launch(context=Dispatchers.IO) {
//            val tdEvs = calendarRepository.getTodayEvents(startDay)
//            _dateEvents.value = tdEvs
//        }
    }

    fun generateDays(dt: LocalDate?) {
        val today = dt?:LocalDate.now()
        val start = today.minusDays(14)
        val end = today.plusDays(21)

        val result = eachWeekUntil(start, end)

        _dates.value = result

        result.forEach { println(it) }
    }

    /** Returns a user-facing error message, or null when the form is valid. */
    private fun validate(state: NewEventPageState): String? = when {
        state.eventName.isBlank() -> "Please add a title"
        state.selectedDates.isEmpty() -> "Please select at least one date"
        !state.isAllDay && state.selectedRecurringType != EventRecurringType.SELECTED_DATES && !state.endTime.isAfter(state.startTime) ->
            "End time must be after the start time"
        !state.isAllDay && state.selectedRecurringType == EventRecurringType.SELECTED_DATES &&
                state.selectedDates.size == 1 && !state.endTime.isAfter(state.startTime) ->
            "End time must be after the start time"
        state.selectedRecurringType != EventRecurringType.NONE &&
                state.repeatUntil != null && state.lastDate != null && state.repeatUntil.isBefore(state.lastDate) ->
            "Repeat end date must be on or after the event date"
        state.selectedRecurringType == EventRecurringType.SPECIFIC_DAYS_WEEKLY && state.selectedDates.isEmpty() ->
            "Select the week days the event should repeat on"
        else -> null
    }

    /**
     * Converts the form state into calendar events.
     *
     * - All day events span whole days ([startOfDay, startOfNextDay)).
     * - Repeating events use a single event with an RRULE on the first date.
     * - Multi-day, non-repeating events create one event per selected date.
     */
    private fun buildEvents(state: NewEventPageState): List<Event> {
        val zone = ZoneId.systemDefault()
        val dates = state.sortedDates
        val untilMillis = state.repeatUntil
            ?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()

        fun startEndFor(date: LocalDate): Pair<Long, Long> =
            if (state.isAllDay) {
                date.atStartOfDay(zone).toInstant().toEpochMilli() to
                        date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            } else {
                date.atTime(state.startTime).atZone(zone).toInstant().toEpochMilli() to
                        date.atTime(state.endTime).atZone(zone).toInstant().toEpochMilli()
            }

        // Weekly repeats keep the picked dates as the week days; the first date is the start.
        val repeatWeekDays = if (state.selectedRecurringType == EventRecurringType.SPECIFIC_DAYS_WEEKLY) {
            dates.map { it.dayOfWeek.value }.distinct()
        } else {
            emptyList()
        }

        fun base(date: LocalDate) = startEndFor(date).let { (start, end) ->
            Event(
                title = state.eventName.trim(),
                location = state.locationLink.ifBlank { null },
                description = state.description.ifBlank { null },
                minutesBefore = if (state.isAllDay) null else state.notifyBeforeTime,
                start = start,
                end = end,
                availability = 0,
                isAllDay = state.isAllDay,
                selectedRecurringType = state.selectedRecurringType,
                customUri = "events/${state.eventCategory.name.lowercase()}",
                repeatWeekDays = repeatWeekDays,
                repeatUntil = untilMillis
            )
        }

        val primaryDate = dates.first()
        return when {
            state.selectedRecurringType == EventRecurringType.NONE ||
                    state.selectedRecurringType == EventRecurringType.SELECTED_DATES -> {
                dates.map { base(it) }
            }
            else -> {
                // One recurring event starting on the first date; RRULE is written by the repository.
                listOf(base(primaryDate))
            }
        }
    }

    private suspend fun newEvent(state: NewEventPageState) {
        try {
            if (!calendarRepository.hasCalendarPermissions()) {
                _newPageEvent.send(NewEventPageEvent.ShowErrorMessage("Calendar permission is required to create events"))
                return
            }
            if (calendarRepository.getDeviceCalendars().isEmpty()) {
                _newPageEvent.send(NewEventPageEvent.ShowErrorMessage("No writable calendar found on this device"))
                return
            }
            val events = buildEvents(state)
            events.forEach { event ->
                val id = calendarRepository.insertEvent(
                    event = event,
                    calendarId = state.selectedCalendarId.takeIf { it > 0 }
                )
                if (id == null) {
                    _newPageEvent.send(NewEventPageEvent.ShowErrorMessage("Could not create event"))
                    return
                }
            }
            _newPageEvent.send(NewEventPageEvent.ShowSuccess)
        } catch (e: Exception) {
            _newPageEvent.send(NewEventPageEvent.ShowErrorMessage("Could not create event: ${e.message}"))
        }
    }

    /** Kept for callers that already build an [Event] themselves. */
    suspend fun newEvent(e: Event) {
        try {
            if (!calendarRepository.hasCalendarPermissions()) {
                _newPageEvent.send(NewEventPageEvent.ShowErrorMessage("Calendar permission is required to create events"))
                return
            }
            val id = calendarRepository.insertEvent(e)
            if (id == null) {
                _newPageEvent.send(NewEventPageEvent.ShowErrorMessage("Could not create event"))
            } else {
                _newPageEvent.send(NewEventPageEvent.ShowSuccess)
            }
        } catch (e: Exception) {
            _newPageEvent.send(NewEventPageEvent.ShowErrorMessage("Could not create event: ${e.message}"))
        }
    }
    fun updateEvent(e: Event){
        viewModelScope.launch(context=Dispatchers.IO) {
            try {
                calendarRepository.updateEvent(event=e)
                _newPageEvent.send(NewEventPageEvent.ShowSuccess)
//                getTodayEvents()
            } catch (e: Exception) {
                Toast.makeText(context, "Could update event: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun deleteEvent(eventId: Long){
        viewModelScope.launch(context=Dispatchers.IO)  {
            try {
                calendarRepository.deleteEvent(eventId)
                _newPageEvent.send(NewEventPageEvent.ShowSuccess)
//                getTodayEvents()
            } catch (e: Exception) {
                Toast.makeText(context, "Could delete event: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun eachWeekUntil(start: LocalDate, end: LocalDate): List<List<LocalDate>> {
        val firstWeekStart = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val days = generateSequence(firstWeekStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(end) }
            .toList()
        return days.chunked(7)
    }
}
