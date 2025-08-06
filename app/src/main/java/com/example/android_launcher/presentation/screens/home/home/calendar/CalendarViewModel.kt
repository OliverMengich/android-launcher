package com.example.android_launcher.presentation.screens.home.calendar

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

class CalendarViewModel: ViewModel() {
    private val _dates = MutableStateFlow< List<List<LocalDate>>>(emptyList())
    val dates: StateFlow<List<List<LocalDate>>> = _dates.asStateFlow()

    fun generateDays() {
        val today = LocalDate.now()
        val start = today.minusDays(14)
        val end = today.plusDays(21)

        val result = eachWeekUntil(start, end)

        _dates.value = result

        result.forEach { println(it) }
    }
}
fun eachWeekUntil(start: LocalDate, end: LocalDate): List<List<LocalDate>> {
    val firstWeekStart = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    val days = generateSequence(firstWeekStart) { it.plusDays(1) }
        .takeWhile { !it.isAfter(end) }
        .toList()
    return days.chunked(7)
//    val firstWeekStart = this.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
//    return generateSequence(firstWeekStart) { it.plusWeeks(1) }
//        .takeWhile { !it.isAfter(end) }
//        .toList()
}