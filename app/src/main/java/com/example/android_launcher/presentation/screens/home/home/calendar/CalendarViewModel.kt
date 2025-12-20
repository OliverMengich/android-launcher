package com.example.android_launcher.presentation.screens.home.home.calendar

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_launcher.domain.models.Event
import com.example.android_launcher.domain.models.EventCategory
import com.example.android_launcher.domain.models.EventRecurringType
import com.example.android_launcher.domain.models.Priority
import com.example.android_launcher.domain.repository.CalendarRepository
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
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import kotlin.random.Random
import kotlin.reflect.KProperty
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
    data class UpdateEventPageValue<T>(
        val field: KProperty1<NewEventPageState,T>,
        val value: T
    ): NewEventPageEvent()
    data object Idle: NewEventPageEvent()
    data object CreateEvent: NewEventPageEvent()
    data class ShowErrorMessage(val message: String): NewEventPageEvent()
    data object ShowSuccess: NewEventPageEvent()
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

    private val _todayEvents = MutableStateFlow<List<Event>>(emptyList())
//    val todayEvents: StateFlow<List<Event>> = _todayEvents.asStateFlow()
    private val startDate = MutableStateFlow<LocalDate>(LocalDate.now())


    val todayEvents = calendarRepository.getTodayEvents(startDay = LocalDate.now()).stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), emptyList())
    val dateEvents = startDate.flatMapLatest { day->
        calendarRepository.getTodayEvents(startDay = day)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), emptyList())

    private val _dateEvents = MutableStateFlow<List<Event>>(emptyList())
//    val dateEvents: StateFlow<List<Event>> = _dateEvents.asStateFlow()

    private val _newPageEvent: Channel<NewEventPageEvent> = Channel()
    val newPageEvent =_newPageEvent.receiveAsFlow()

//    init {
//        getTodayEvents()
//    }
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
                            title =eventName,
                            location = locationLink,
                            description = description,
                            notesPath = "",
                            startTime = startTime,
                            endTime = endTime,
                            notesUrl = "",
                            eventCategory = eventCategory,
                            priority = selectedPriority,
                            dates = finalDates,
                            recurringType = selectedRecurringType,
                            notifyBeforeTime = notifyBeforeTime
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
    fun deleteEvent(eventId: Int){
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
