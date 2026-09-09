package com.planara.android_launcher.presentation.screens.home.home.calendar

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.planara.android_launcher.domain.models.DeviceCalendar
import com.planara.android_launcher.domain.models.EventCategory
import com.planara.android_launcher.domain.models.EventRecurringType
import com.planara.android_launcher.domain.models.Priority
import com.planara.android_launcher.presentation.components.MultiDatePicker
import com.planara.android_launcher.presentation.screens.home.SharedViewModel
import com.planara.android_launcher.utils.formatGivenDateToRequiredFormat
import com.planara.android_launcher.utils.formatTimeToRequiredFormat
import com.planara.android_launcher.utils.notifyOptions
import org.koin.compose.viewmodel.koinViewModel
import kotlin.reflect.KProperty1
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

private const val REPEAT_NEVER = 0

/** Repeat presets, similar to Google Calendar / Outlook. */
private enum class RepeatPreset(val label: String, val recurring: EventRecurringType) {
    NEVER("Does not repeat", EventRecurringType.NONE),
    DAILY("Daily", EventRecurringType.DAILY),
    WEEKDAYS("Every week day (Mon–Fri)", EventRecurringType.WEEK_DAYS),
    WEEKLY("Weekly on selected days", EventRecurringType.SPECIFIC_DAYS_WEEKLY),
    SELECTED_DATES("On selected dates only", EventRecurringType.SELECTED_DATES)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEventPage(
    viewModel: CalendarViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinViewModel(),
    goBack: () -> Unit
) {
    val uiState by viewModel.newEventPageState.collectAsState()
    val newPageEvent by viewModel.newPageEvent.collectAsState(initial = NewEventPageEvent.Idle)
    val context = LocalContext.current
    val localManagerData by sharedViewModel.localManagerData.collectAsStateWithLifecycle()
    val tmFmt = localManagerData.displaySettings.timeFormat

    val deviceCalendars by viewModel.deviceCalendars.collectAsState()
    var hasCalendarPermission by remember { mutableStateOf(viewModel.hasCalendarPermission) }

    var showTimePicker by remember { mutableStateOf<String?>(null) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var showRepeatEndPicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val timePickerState = rememberTimePickerState()

    // Runtime permission request, in case the user creates an event before granting access.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { results ->
            hasCalendarPermission = results.values.all { it }
            if (hasCalendarPermission) {
                viewModel.getWritableCalendars()
            } else {
                Toast.makeText(context, "Calendar permission is required to create events", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCalendarPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
            )
        } else {
            viewModel.getWritableCalendars()
        }
    }

    // Default the per-event calendar to the saved default (if any) once both are known.
    LaunchedEffect(deviceCalendars.size, localManagerData.selectedDeviceCalendar) {
        if (uiState.selectedCalendarId == 0L && deviceCalendars.isNotEmpty()) {
            val preferred = deviceCalendars.firstOrNull { it.id == localManagerData.selectedDeviceCalendar }
                ?: deviceCalendars.first()
            viewModel.onEvent(
                NewEventPageEvent.UpdateEventPageValue(NewEventPageState::selectedCalendarId, preferred.id)
            )
        }
    }

    LaunchedEffect(newPageEvent) {
        when (val event = newPageEvent) {
            is NewEventPageEvent.ShowSuccess -> {
                Toast.makeText(context, "Event added to your calendar", Toast.LENGTH_SHORT).show()
                goBack()
            }
            is NewEventPageEvent.ShowErrorMessage -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    fun <T> updateValue(field: KProperty1<NewEventPageState, T>, value: T) {
        viewModel.onEvent(NewEventPageEvent.UpdateEventPageValue(field = field, value = value))
    }

    fun setDateRange(start: LocalDate, end: LocalDate) {
        val days = generateSequence(start) { it.plusDays(1) }
            .takeWhile { !it.isAfter(end) }
            .take(366)
            .toSet()
        updateValue(NewEventPageState::selectedDates, days)
    }

    // ---------- Dialogs ----------
    if (showTimePicker != null) {
        BasicAlertDialog(
            onDismissRequest = { showTimePicker = null },
            content = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
                ) {
                    Text(
                        text = if (showTimePicker == "start") "Start time" else "End time",
                        modifier = Modifier.padding(top = 12.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    TimePicker(state = timePickerState)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val picked = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            if (showTimePicker == "start") {
                                updateValue(NewEventPageState::startTime, picked)
                            } else {
                                updateValue(NewEventPageState::endTime, picked)
                            }
                            showTimePicker = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
                    ) {
                        Text(
                            "OK",
                            modifier = Modifier.padding(10.dp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.background
                        )
                    }
                }
            }
        )
    }

    if (showDateRangePicker) {
        val rangeState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val startMillis = rangeState.selectedStartDateMillis
                        val endMillis = rangeState.selectedEndDateMillis ?: startMillis
                        if (startMillis != null && endMillis != null) {
                            val zone = ZoneId.systemDefault()
                            val start = Instant.ofEpochMilli(startMillis).atZone(zone).toLocalDate()
                            val end = Instant.ofEpochMilli(endMillis).atZone(zone).toLocalDate()
                            setDateRange(start, end)
                        }
                        showDateRangePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(
                state = rangeState,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    }

    if (showRepeatEndPicker) {
        val repeatEndState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = uiState.repeatUntil
                ?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showRepeatEndPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = repeatEndState.selectedDateMillis
                        updateValue(
                            NewEventPageState::repeatUntil,
                            millis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                        )
                        showRepeatEndPicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRepeatEndPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state = repeatEndState,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    }

    // ---------- Screen ----------
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp, end = 15.dp, start = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = goBack) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = "close")
            }
            Text(text = "New event", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Button(
                onClick = { viewModel.onEvent(NewEventPageEvent.CreateEvent) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
            ) {
                Text(text = "Save", color = MaterialTheme.colorScheme.background)
            }
        }
        HorizontalDivider()

        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(.95f)
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp, end = 15.dp, start = 15.dp)
        ) {
            if (!hasCalendarPermission) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calendar access is off, so events can't be saved.",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    TextButton(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
                            )
                        }
                    ) { Text("Allow") }
                }
            }

            // Title
            OutlinedTextField(
                value = uiState.eventName,
                onValueChange = { updateValue(NewEventPageState::eventName, it) },
                placeholder = { Text("Add title") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrectEnabled = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                trailingIcon = {
                    if (uiState.eventName.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            modifier = Modifier.clickable { updateValue(NewEventPageState::eventName, "") },
                            contentDescription = "Clear title"
                        )
                    }
                },
            )

            // Category chips
            LazyRow(
                Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(EventCategory.entries) { category ->
                    InputChip(
                        label = { Text(category.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                        onClick = { updateValue(NewEventPageState::eventCategory, category) },
                        leadingIcon = if (uiState.eventCategory == category) {
                            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                        } else null,
                        shape = RoundedCornerShape(20.dp),
                        selected = uiState.eventCategory == category
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            // All day
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("All day", fontWeight = FontWeight.Medium)
                    Text("Event lasts the whole day", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
                Switch(
                    checked = uiState.isAllDay,
                    onCheckedChange = { updateValue(NewEventPageState::isAllDay, it) }
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            // Date range
            Row(
                Modifier.fillMaxWidth().clickable { showDateRangePicker = true },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Date", fontWeight = FontWeight.Medium)
                Text(
                    text = when {
                        uiState.selectedDates.isEmpty() -> "Select dates"
                        uiState.firstDate == uiState.lastDate ->
                            formatGivenDateToRequiredFormat(uiState.firstDate!!, localManagerData.displaySettings.dateFormat)
                        else ->
                            "${formatGivenDateToRequiredFormat(uiState.firstDate!!, localManagerData.displaySettings.dateFormat)} – " +
                                    formatGivenDateToRequiredFormat(uiState.lastDate!!, localManagerData.displaySettings.dateFormat)
                    },
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
            if (uiState.selectedDates.size > 1) {
                Text(
                    text = "${uiState.selectedDates.size} days selected",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            // Times
            if (!uiState.isAllDay) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.startTime.formatTimeToRequiredFormat(tmFmt),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Starts") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    OutlinedTextField(
                        value = uiState.endTime.formatTimeToRequiredFormat(tmFmt),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ends") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = {
                            timePickerState.hour = uiState.startTime.hour
                            timePickerState.minute = uiState.startTime.minute
                            showTimePicker = "start"
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Change start") }
                    TextButton(
                        onClick = {
                            timePickerState.hour = uiState.endTime.hour
                            timePickerState.minute = uiState.endTime.minute
                            showTimePicker = "end"
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Change end") }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            // Repeat
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Repeat", fontWeight = FontWeight.Medium)
                RepeatPresetDropdown(
                    value = uiState.selectedRecurringType,
                    onSelect = { preset -> updateValue(NewEventPageState::selectedRecurringType, preset.recurring) }
                )

                val repeats = uiState.selectedRecurringType != EventRecurringType.NONE
                if (repeats) {
                    Row(
                        Modifier.fillMaxWidth().clickable { showRepeatEndPicker = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ends", fontWeight = FontWeight.Medium)
                        Text(
                            text = uiState.repeatUntil?.let {
                                formatGivenDateToRequiredFormat(it, localManagerData.displaySettings.dateFormat)
                            } ?: "Never",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }
                    if (uiState.repeatUntil != null) {
                        TextButton(onClick = { updateValue(NewEventPageState::repeatUntil, null) }) {
                            Text("Repeat forever")
                        }
                    }
                }

                if (uiState.selectedRecurringType == EventRecurringType.SPECIFIC_DAYS_WEEKLY) {
                    Text(
                        text = "Pick the days below; the event repeats weekly on those days.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            // Date picker
            Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Text("Select dates", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                MultiDatePicker(
                    selectedDates = uiState.selectedDates,
                    onDatesSelected = { updateValue(NewEventPageState::selectedDates, it) },
                    maxSelectableDates = 31,
                    minDate = LocalDate.now().minusDays(1)
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            // Calendar / account
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Calendar", fontWeight = FontWeight.Medium)
                CalendarDropdown(
                    calendars = deviceCalendars,
                    selectedId = uiState.selectedCalendarId,
                    onSelect = { updateValue(NewEventPageState::selectedCalendarId, it) }
                )
                Text(
                    text = "Choose the account this event is saved to. It will show up in that calendar everywhere.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            // Priority
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Priority", fontWeight = FontWeight.Medium)
                ExposedDropdownMenuExample(
                    options = Priority.entries,
                    value = uiState.selectedPriority,
                    setValue = { updateValue(NewEventPageState::selectedPriority, it) },
                    displayText = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            // Notification
            if (!uiState.isAllDay) {
                Column(Modifier.padding(vertical = 0.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = "notifications")
                        Spacer(Modifier.size(6.dp))
                        Text(text = notifyOptions[uiState.notifyBeforeTime] ?: "No reminder")
                    }
                    LazyRow(verticalAlignment = Alignment.CenterVertically) {
                        items(notifyOptions.keys.toList()) { minutes ->
                            InputChip(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                                label = { Text(text = notifyOptions[minutes] ?: "") },
                                selected = uiState.notifyBeforeTime == minutes,
                                onClick = { updateValue(NewEventPageState::notifyBeforeTime, minutes) },
                            )
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
            }

            // Location
            Column(Modifier.padding(bottom = 0.dp)) {
                Text("Location or link", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                TextField(
                    value = uiState.locationLink,
                    onValueChange = { updateValue(NewEventPageState::locationLink, it) },
                    placeholder = { Text("Add location or meeting link") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            // Notes
            Column(Modifier.padding(bottom = 0.dp)) {
                Text("Notes", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { updateValue(NewEventPageState::description, it) },
                    placeholder = { Text("Add description") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp, max = 300.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatPresetDropdown(
    value: EventRecurringType,
    onSelect: (RepeatPreset) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = RepeatPreset.entries.firstOrNull { it.recurring == value } ?: RepeatPreset.NEVER

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = current.label,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            RepeatPreset.entries.forEach { preset ->
                DropdownMenuItem(
                    text = { Text(preset.label) },
                    onClick = {
                        onSelect(preset)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarDropdown(
    calendars: List<DeviceCalendar>,
    selectedId: Long,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = calendars.firstOrNull { it.id == selectedId } ?: calendars.firstOrNull()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (calendars.isNotEmpty()) expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected?.displayLabel ?: "No calendars found",
            onValueChange = {},
            readOnly = true,
            leadingIcon = {
                Box(
                    Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            selected?.color?.takeIf { it != 0 }?.let { Color(it) }
                                ?: MaterialTheme.colorScheme.onBackground
                        )
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            calendars.forEach { calendar ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        calendar.color.takeIf { it != 0 }?.let { Color(it) }
                                            ?: MaterialTheme.colorScheme.onBackground
                                    )
                            )
                            Spacer(Modifier.size(8.dp))
                            Column {
                                Text(calendar.name)
                                if (calendar.accountName.isNotBlank() && calendar.accountName != calendar.name) {
                                    Text(
                                        text = calendar.accountName,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    },
                    onClick = {
                        onSelect(calendar.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ExposedDropdownMenuExample(
    modifier: Modifier = Modifier,
    value: T,
    setValue: (T) -> Unit,
    options: List<T>,
    label: String = "Select an Option",
    displayText: (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = displayText(value),
            onValueChange = { },
            readOnly = true,
            label = { if (label.isNotBlank()) Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(displayText(option)) },
                    onClick = {
                        setValue(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
