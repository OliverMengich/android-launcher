package com.example.android_launcher.presentation.components

// 1. Data Classes and State Management
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

data class CalendarDay(
    val date: LocalDate,
    val isFromCurrentMonth: Boolean,
    val isSelected: Boolean,
    val isToday: Boolean
)

data class CalendarState(
    val selectedDates: Set<LocalDate> = emptySet(),
    val currentMonth: YearMonth = YearMonth.now(),
    val today: LocalDate = LocalDate.now()
)

// 2. Main Multi-Date Picker Composable
@Composable
fun MultiDatePicker(
    modifier: Modifier = Modifier,
    selectedDates: Set<LocalDate>,
    onDatesSelected: (Set<LocalDate>) -> Unit,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    maxSelectableDates: Int = Int.MAX_VALUE,
    disabledDates: Set<LocalDate> = emptySet(),
    enableAllDays: Boolean?=false,
    enableWeekDays: Boolean?=false,
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(vertical=16.dp)) {
            // Header with month navigation
            CalendarHeader(
                currentMonth = currentMonth,
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
            )

            Spacer(Modifier.height(16.dp))

            // Calendar grid
            CalendarGrid(
                currentMonth = currentMonth,
                enableAllDays = enableAllDays,
                enableWeekDays = enableWeekDays,
                selectedDates = selectedDates,
                onDateSelected = { date ->
                    val newSelectedDates = if (selectedDates.contains(date)) {
                        selectedDates - date
                    } else if (selectedDates.size < maxSelectableDates) {
                        selectedDates + date
                    } else {
                        selectedDates // Don't add if max limit reached
                    }
                    onDatesSelected(newSelectedDates)
                },
                minDate = minDate,
                maxDate = maxDate,
                disabledDates = disabledDates
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

// 3. Calendar Header Component
@Composable
fun CalendarHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous month"
            )
        }

        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next month"
            )
        }
    }
}

// 4. Calendar Grid Component
@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    minDate: LocalDate?,
    enableAllDays: Boolean?=false,
    enableWeekDays: Boolean?=false,
    maxDate: LocalDate?,
    disabledDates: Set<LocalDate>
) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val calendarDays = generateCalendarDays(currentMonth, selectedDates)

    Column {
        // Days of week header
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items(daysOfWeek) { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar days grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(300.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(calendarDays) { calendarDay ->
                CalendarDayCell(
                    enableAllDays = enableAllDays,
                    enableWeekDays = enableWeekDays,
                    calendarDay = calendarDay,
                    onClick = {
                        if (calendarDay.isFromCurrentMonth &&
                            isDateSelectable(calendarDay.date, minDate, maxDate, disabledDates)) {
                            onDateSelected(calendarDay.date)
                        }
                    }
                )
            }
        }
    }
}

// 5. Individual Day Cell Component
@Composable
fun CalendarDayCell(
    enableAllDays: Boolean?=false,
    enableWeekDays: Boolean?=false,
    calendarDay: CalendarDay,
    onClick: () -> Unit
) {

    val isWeekday = calendarDay.date.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

    val shouldHighlight = calendarDay.isSelected ||
            enableAllDays == true ||
            (enableWeekDays == true && isWeekday)

    val backgroundColor = when {
        shouldHighlight -> MaterialTheme.colorScheme.onBackground
        calendarDay.isToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    val textColor = when {
        shouldHighlight -> MaterialTheme.colorScheme.background
        !calendarDay.isFromCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(size=40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(enabled = calendarDay.isFromCurrentMonth) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = calendarDay.date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (calendarDay.isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// 6. Selected Dates Section
@Composable
fun SelectedDatesSection(
    selectedDates: Set<LocalDate>,
    onDateRemoved: (LocalDate) -> Unit,
    onClearAll: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Selected Dates (${selectedDates.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onClearAll) {
                Text("Clear All")
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(selectedDates.sorted()) { date ->
                SelectedDateChip(
                    date = date,
                    onRemove = { onDateRemoved(date) }
                )
            }
        }
    }
}

// 7. Selected Date Chip Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedDateChip(
    date: LocalDate,
    onRemove: () -> Unit
) {
    InputChip(
        onClick = onRemove,
        label = {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("MMM dd")),
                fontSize = 12.sp
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Remove date",
                modifier = Modifier.size(16.dp)
            )
        },
        selected = false,
        modifier = Modifier.height(32.dp)
    )
}

// 8. Utility Functions
fun generateCalendarDays(
    month: YearMonth,
    selectedDates: Set<LocalDate>
): List<CalendarDay> {
    val firstDayOfMonth = month.atDay(1)
    val lastDayOfMonth = month.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val today = LocalDate.now()

    val days = mutableListOf<CalendarDay>()

    // Previous month days
    val previousMonth = month.minusMonths(1)
    val daysFromPreviousMonth = firstDayOfWeek
    for (i in daysFromPreviousMonth downTo 1) {
        val date = previousMonth.atEndOfMonth().minusDays((i - 1).toLong())
        days.add(
            CalendarDay(
                date = date,
                isFromCurrentMonth = false,
                isSelected = selectedDates.contains(date),
                isToday = date == today
            )
        )
    }

    // Current month days
    for (day in 1..lastDayOfMonth.dayOfMonth) {
        val date = month.atDay(day)
        days.add(
            CalendarDay(
                date = date,
                isFromCurrentMonth = true,
                isSelected = selectedDates.contains(date),
                isToday = date == today
            )
        )
    }

    // Next month days
    val remainingCells = 42 - days.size // 6 weeks * 7 days
    val nextMonth = month.plusMonths(1)
    for (day in 1..remainingCells) {
        val date = nextMonth.atDay(day)
        days.add(
            CalendarDay(
                date = date,
                isFromCurrentMonth = false,
                isSelected = selectedDates.contains(date),
                isToday = date == today
            )
        )
    }

    return days
}

fun isDateSelectable(
    date: LocalDate,
    minDate: LocalDate?,
    maxDate: LocalDate?,
    disabledDates: Set<LocalDate>
): Boolean {
    return !disabledDates.contains(date) &&
            (minDate == null || date >= minDate) &&
            (maxDate == null || date <= maxDate)
}

// 9. Date Range Picker Variant
@Composable
fun MultiDateRangePicker(
    selectedRanges: List<Pair<LocalDate, LocalDate>>,
    onRangesChanged: (List<Pair<LocalDate, LocalDate>>) -> Unit,
    modifier: Modifier = Modifier
) {
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Select Date Ranges",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            CalendarHeader(
                currentMonth = currentMonth,
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Range selection status
            RangeSelectionStatus(
                startDate = startDate,
                endDate = endDate,
                onClear = {
                    startDate = null
                    endDate = null
                },
                onConfirm = {
                    if (startDate != null && endDate != null) {
                        val newRange = Pair(
                            minOf(startDate!!, endDate!!),
                            maxOf(startDate!!, endDate!!)
                        )
                        onRangesChanged(selectedRanges + newRange)
                        startDate = null
                        endDate = null
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar for range selection
            DateRangeCalendarGrid(
                currentMonth = currentMonth,
                selectedRanges = selectedRanges,
                startDate = startDate,
                endDate = endDate,
                onDateSelected = { date ->
                    when {
                        startDate == null -> startDate = date
                        endDate == null && date != startDate -> endDate = date
                        else -> {
                            startDate = date
                            endDate = null
                        }
                    }
                }
            )

            // Show selected ranges
            if (selectedRanges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SelectedRangesSection(
                    ranges = selectedRanges,
                    onRangeRemoved = { rangeToRemove ->
                        onRangesChanged(selectedRanges - rangeToRemove)
                    }
                )
            }
        }
    }
}

@Composable
fun RangeSelectionStatus(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onClear: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = when {
                    startDate == null -> "Select start date"
                    endDate == null -> "Select end date"
                    else -> "Range ready"
                },
                style = MaterialTheme.typography.bodySmall
            )
            if (startDate != null || endDate != null) {
                val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")
                Text(
                    text = "${startDate?.format(dateFormatter) ?: "Start"} - ${endDate?.format(dateFormatter) ?: "End"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row {
            if (startDate != null || endDate != null) {
                TextButton(onClick = onClear) {
                    Text("Clear")
                }
            }
            if (startDate != null && endDate != null) {
                Button(onClick = onConfirm) {
                    Text("Add Range")
                }
            }
        }
    }
}

@Composable
fun DateRangeCalendarGrid(
    currentMonth: YearMonth,
    selectedRanges: List<Pair<LocalDate, LocalDate>>,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val calendarDays = generateRangeCalendarDays(currentMonth, selectedRanges, startDate, endDate)

    Column {
        // Days of week header
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items(daysOfWeek) { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(300.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(calendarDays) { day ->
                RangeCalendarDayCell(
                    day = day,
                    onClick = { onDateSelected(day.date) }
                )
            }
        }
    }
}

data class RangeCalendarDay(
    val date: LocalDate,
    val isFromCurrentMonth: Boolean,
    val isInSelectedRange: Boolean,
    val isRangeStart: Boolean,
    val isRangeEnd: Boolean,
    val isTemporaryStart: Boolean,
    val isTemporaryEnd: Boolean,
    val isToday: Boolean
)

@Composable
fun RangeCalendarDayCell(
    day: RangeCalendarDay,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        day.isRangeStart || day.isRangeEnd || day.isTemporaryStart || day.isTemporaryEnd ->
            MaterialTheme.colorScheme.primary
        day.isInSelectedRange -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        day.isToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    val textColor = when {
        day.isRangeStart || day.isRangeEnd || day.isTemporaryStart || day.isTemporaryEnd ->
            MaterialTheme.colorScheme.onPrimary
        !day.isFromCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(enabled = day.isFromCurrentMonth) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (day.isRangeStart || day.isRangeEnd || day.isTemporaryStart || day.isTemporaryEnd)
                FontWeight.Bold else FontWeight.Normal
        )
    }
}

fun generateRangeCalendarDays(
    month: YearMonth,
    selectedRanges: List<Pair<LocalDate, LocalDate>>,
    startDate: LocalDate?,
    endDate: LocalDate?
): List<RangeCalendarDay> {
    val firstDayOfMonth = month.atDay(1)
    val lastDayOfMonth = month.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val today = LocalDate.now()

    val days = mutableListOf<RangeCalendarDay>()

    // Previous month days
    val previousMonth = month.minusMonths(1)
    val daysFromPreviousMonth = firstDayOfWeek
    for (i in daysFromPreviousMonth downTo 1) {
        val date = previousMonth.atEndOfMonth().minusDays((i - 1).toLong())
        days.add(createRangeCalendarDay(date, false, selectedRanges, startDate, endDate, today))
    }

    // Current month days
    for (day in 1..lastDayOfMonth.dayOfMonth) {
        val date = month.atDay(day)
        days.add(createRangeCalendarDay(date, true, selectedRanges, startDate, endDate, today))
    }

    // Next month days
    val remainingCells = 42 - days.size
    val nextMonth = month.plusMonths(1)
    for (day in 1..remainingCells) {
        val date = nextMonth.atDay(day)
        days.add(createRangeCalendarDay(date, false, selectedRanges, startDate, endDate, today))
    }

    return days
}

fun createRangeCalendarDay(
    date: LocalDate,
    isFromCurrentMonth: Boolean,
    selectedRanges: List<Pair<LocalDate, LocalDate>>,
    startDate: LocalDate?,
    endDate: LocalDate?,
    today: LocalDate
): RangeCalendarDay {
    val isInSelectedRange = selectedRanges.any { (start, end) ->
        date >= start && date <= end
    }
    val isRangeStart = selectedRanges.any { (start, _) -> date == start }
    val isRangeEnd = selectedRanges.any { (_, end) -> date == end }

    val tempStart = startDate
    val tempEnd = endDate
    val isTemporaryStart = date == tempStart
    val isTemporaryEnd = date == tempEnd

    return RangeCalendarDay(
        date = date,
        isFromCurrentMonth = isFromCurrentMonth,
        isInSelectedRange = isInSelectedRange,
        isRangeStart = isRangeStart,
        isRangeEnd = isRangeEnd,
        isTemporaryStart = isTemporaryStart,
        isTemporaryEnd = isTemporaryEnd,
        isToday = date == today
    )
}

@Composable
fun SelectedRangesSection(
    ranges: List<Pair<LocalDate, LocalDate>>,
    onRangeRemoved: (Pair<LocalDate, LocalDate>) -> Unit
) {
    Column {
        Text(
            text = "Selected Ranges (${ranges.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.heightIn(max = 150.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(ranges) { range ->
                SelectedRangeItem(
                    range = range,
                    onRemove = { onRangeRemoved(range) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedRangeItem(
    range: Pair<LocalDate, LocalDate>,
    onRemove: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${range.first.format(dateFormatter)} - ${range.second.format(dateFormatter)}",
                style = MaterialTheme.typography.bodyMedium
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Remove range",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// 10. Usage Example
@Composable
fun MultiDatePickerExample() {
    var selectedDates by remember { mutableStateOf(setOf<LocalDate>()) }
    var selectedRanges by remember { mutableStateOf(listOf<Pair<LocalDate, LocalDate>>()) }
    var showRangePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { showRangePicker = false },
                colors = if (!showRangePicker) ButtonDefaults.buttonColors()
                else ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Multi-Date")
            }

            Button(
                onClick = { showRangePicker = true },
                colors = if (showRangePicker) ButtonDefaults.buttonColors()
                else ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Date Ranges")
            }
        }

        if (showRangePicker) {
            MultiDateRangePicker(
                selectedRanges = selectedRanges,
                onRangesChanged = { selectedRanges = it },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            MultiDatePicker(
                selectedDates = selectedDates,
                onDatesSelected = { selectedDates = it },
                modifier = Modifier.fillMaxWidth(),
                maxSelectableDates = 10,
                minDate = LocalDate.now(),
                disabledDates = setOf(
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(12)
                )
            )
        }
    }
}