package com.example.android_launcher.presentation.screens.home.home.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_launcher.domain.models.Event
import com.example.android_launcher.domain.models.EventCategory
import com.example.android_launcher.domain.models.EventRecurringType
import com.example.android_launcher.domain.models.Priority
import com.example.android_launcher.domain.repository.CalendarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import kotlin.random.Random
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

data class NewEventPageState(
    val eventName: String="",
    val locationLink: String = "",
    val description: String = "",
    val isAllDay: Boolean = false,
    val selectedDates: Set<LocalDate> = emptySet<LocalDate>(),
    val startTime: LocalTime= LocalTime.now().plusMinutes(30),
    val endTime: LocalTime= LocalTime.now().plusHours(1),
    val eventCategory: EventCategory = EventCategory.EVENT,
    val selectedPriority: Priority = Priority.LOW,
    val selectedRecurringType : EventRecurringType = EventRecurringType.NONE
)
sealed class NewEventPageEvent{
    data class UpdateEventPageValue<T>(
        val field: KProperty1<NewEventPageState,T>,
        val value: T
    ): NewEventPageEvent()
    data object Idle: NewEventPageEvent()
    data object CreateEvent: NewEventPageEvent()
    data class ShowErrorMessage(val message: String): NewEventPageEvent()
    data object ShowSuccess: NewEventPageEvent()

}

class CalendarViewModel(private val calendarRepository: CalendarRepository): ViewModel() {
    private val _newEventPageState = MutableStateFlow<NewEventPageState>(NewEventPageState())
    val newEventPageState: StateFlow<NewEventPageState> = _newEventPageState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = NewEventPageState()
    )
    private val _dates = MutableStateFlow< List<List<LocalDate>>>(emptyList())
    val dates: StateFlow<List<List<LocalDate>>> = _dates.asStateFlow()

    private val _todayEvents = MutableStateFlow<List<Event>>(emptyList())
    val todayEvents: StateFlow<List<Event>> = _todayEvents.asStateFlow()

    private val _newPageEvent: Channel<NewEventPageEvent> = Channel()
    val newPageEvent =_newPageEvent.receiveAsFlow()

    init {
        viewModelScope.launch(context=Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            val startDay = calendar.apply {
                set(Calendar.HOUR_OF_DAY,0)
                set(Calendar.MINUTE,0)
                set(Calendar.SECOND,0)
                set(Calendar.SECOND,0)
                set(Calendar.MILLISECOND,0)
            }.timeInMillis
            getTodayEvents(startDay)
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
                        NewEventPageState::isAllDay -> currentState.copy(isAllDay = event.value as Boolean)
                        NewEventPageState::selectedDates -> currentState.copy(selectedDates = event.value as Set<LocalDate>)
                        NewEventPageState::selectedPriority -> currentState.copy(selectedPriority = event.value as Priority)
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
                    EventRecurringType.SPECIFIC_DATES -> {
                        newEventPageState.value.selectedDates.map { it.toString() }
                    }
                }
                viewModelScope.launch {
                    val (eventName, locationLink, description, isAllDay, selectedDates,startTime,endTime, eventCategory, selectedPriority, selectedRecurringType) = newEventPageState.value
                    newEvent(
                        e=Event(
                            id = Random.nextInt(0,1000_000),
                            title =eventName,
                            location = locationLink,
                            description = description.take(n=20),
                            notesPath = "",
                            startTime = startTime?.toSecondOfDay()?.toLong() ,
                            endTime = endTime?.toSecondOfDay()?.toLong(),
                            notesUrl = "",
                            eventCategory = eventCategory,
                            priority = Priority.LOW,
                            dates = finalDates,
                            isPassed = false,
                            recurringType = selectedRecurringType,
                        )
                    )
                }
            }
            else -> {}
        }
    }
    suspend fun getTodayEvents(startDay: Long){
        val tdEvs = calendarRepository.getTodayEvents(startDay)
        _todayEvents.value = tdEvs
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
        }catch (e: Exception){
            _newPageEvent.send(NewEventPageEvent.ShowErrorMessage("Could create event: ${e.message}"))
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
