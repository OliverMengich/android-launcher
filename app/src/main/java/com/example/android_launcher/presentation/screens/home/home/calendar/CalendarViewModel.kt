package com.example.android_launcher.presentation.screens.home.home.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_launcher.domain.models.Event
import com.example.android_launcher.domain.repository.CalendarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Calendar

class CalendarViewModel(private val calendarRepository: CalendarRepository): ViewModel() {
    private val _dates = MutableStateFlow< List<List<LocalDate>>>(emptyList())
    val dates: StateFlow<List<List<LocalDate>>> = _dates.asStateFlow()

    private val _todayEvents = MutableStateFlow<List<Event>>(emptyList())
    val todayEvents: StateFlow<List<Event>> = _todayEvents.asStateFlow()

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
        calendarRepository.insertEvent(e)
    }
    private fun eachWeekUntil(start: LocalDate, end: LocalDate): List<List<LocalDate>> {
        val firstWeekStart = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val days = generateSequence(firstWeekStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(end) }
            .toList()
        return days.chunked(7)
    }
}
