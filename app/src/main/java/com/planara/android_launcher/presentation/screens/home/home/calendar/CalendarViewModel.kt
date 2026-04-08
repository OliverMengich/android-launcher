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
    val eventName: String="",
    val locationLink: String = "",
    val description: String = "",
    val notifyBeforeTime: Int=30,
    val selectedDates: Set<LocalDate> = emptySet<LocalDate>(),
    val startTime: LocalTime= LocalTime.now().plusMinutes(30),
    val endTime: LocalTime= LocalTime.now().plusHours(1),
    val eventCategory: EventCategory = EventCategory.EVENT,
    val selectedPriority: Priority = Priority.LOW,
    val selectedRecurringType : EventRecurringType = EventRecurringType.SELECTED_DATES
)
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
    val todayEvents = calendarRepository
        .getTodayEvents(startDay = LocalDate.now())
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = emptyList()
        )
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
                        NewEventPageState::selectedRecurringType -> currentState.copy(selectedRecurringType =  event.value as EventRecurringType)
                        else -> currentState
                    }
                }
            }
            is NewEventPageEvent.CreateEvent -> {
                val finalDates= when(newEventPageState.value.selectedRecurringType){
                    EventRecurringType.SPECIFIC_DAYS_WEEKLY -> {
                        newEventPageState.value.selectedDates.map{dt->
                            dt.dayOfWeek.toString()
                        }
                    }
                    EventRecurringType.NONE,
                    EventRecurringType.DAILY,
                    EventRecurringType.WEEK_DAYS,
                    EventRecurringType.SELECTED_DATES -> {
                        newEventPageState.value.selectedDates.map { it.toString() }
                    }
                }
                viewModelScope.launch {
                    val (eventName, locationLink, description, notifyBeforeTime, _, startTime,endTime, eventCategory, selectedPriority, selectedRecurringType) = newEventPageState.value
                    Toast.makeText(context, "Creating event $selectedPriority", Toast.LENGTH_SHORT).show()
                    newEvent(
                        e=Event(
                            title =  newEventPageState.value.eventName,
                            location =  newEventPageState.value.locationLink,
                            description =  newEventPageState.value.description,
                            minutesBefore =  newEventPageState.value.notifyBeforeTime,
                            start = localTimeToMillisToday(newEventPageState.value.startTime),
                            end = localTimeToMillisToday(newEventPageState.value.endTime),
                            availability = 0,
                            selectedRecurringType = newEventPageState.value.selectedRecurringType,
                            isAllDay =  newEventPageState.value.selectedRecurringType == EventRecurringType.DAILY,
                            customUri = "",
                        )
                    )
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

    suspend fun newEvent(e: Event){
        try {
            calendarRepository.insertEvent(e)
            _newPageEvent.send(NewEventPageEvent.ShowSuccess)
//            getTodayEvents()
        }catch (e: Exception){
            _newPageEvent.send(NewEventPageEvent.ShowErrorMessage("Could create event: ${e.message}"))
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
