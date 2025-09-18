package com.example.android_launcher.presentation.screens.home.home.calendar

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_launcher.domain.models.Event
import com.example.android_launcher.domain.models.EventCategory
import com.example.android_launcher.domain.models.EventRecurringType
import com.example.android_launcher.domain.models.Priority
import com.example.android_launcher.presentation.components.MultiDatePicker
import com.example.android_launcher.utils.formatTimeFromMillis
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEventPage(viewModel: CalendarViewModel = koinViewModel(), goBack:()->Unit) {
    val datePickerState = rememberDatePickerState()
    val uiState by viewModel.newEventPageState.collectAsState()
    val newPageEvent by viewModel.newPageEvent.collectAsState(initial = NewEventPageEvent.Idle)

    val context= LocalContext.current
    val scrollState = rememberScrollState()
    val rowScrollState = rememberScrollState()
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
    )
    val scope = rememberCoroutineScope()
    var showTimeDialog by remember{mutableStateOf(false)}
    var category by remember{mutableStateOf<String?>(null)}
    LaunchedEffect(newPageEvent) {
        when(newPageEvent){
            is NewEventPageEvent.ShowSuccess->{
                Toast.makeText(context,"Successfully added",Toast.LENGTH_SHORT).show()
                goBack()
            }
            is NewEventPageEvent.ShowErrorMessage->{
                Toast.makeText(context,(newPageEvent as NewEventPageEvent.ShowErrorMessage).message,Toast.LENGTH_SHORT).show()
            }
            else -> {

            }
        }
    }
    fun onTimePicked(){
        if (category==null){
            return
        }
        showTimeDialog=false
        when(category){
            "startTime"->{
                viewModel.onEvent(
                    event=NewEventPageEvent.UpdateEventPageValue(
                        field=NewEventPageState::startTime,
                        value= LocalTime.of(timePickerState.hour, timePickerState.minute)
                    )
                )
            }
            "endTime"->{
                viewModel.onEvent(
                    event=NewEventPageEvent.UpdateEventPageValue(
                        field=NewEventPageState::endTime,
                        value= LocalTime.of(timePickerState.hour, timePickerState.minute)
                    )
                )
            }
        }
        category=null
    }
    fun createEventHandler(){
        viewModel.onEvent(
            event=NewEventPageEvent.CreateEvent
        )
    }
    LaunchedEffect(Unit){
        viewModel.onEvent(
            event=NewEventPageEvent.UpdateEventPageValue(
                field=NewEventPageState::startTime,
                value= LocalTime.of(timePickerState.hour, timePickerState.minute)
            )
        )
    }

    if(showTimeDialog){
        BasicAlertDialog(
            onDismissRequest = {showTimeDialog=false},
            content = {
                Column (horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier=Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
                    TimePicker(state = timePickerState)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {onTimePicked()},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                        ),
                        content = {
                            Text("OK",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.background,
                            )
                        }
                    )
                }
            }
        )
    }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 10.dp, end = 15.dp, start = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick=goBack) {
                Icon(imageVector=Icons.Default.Clear, contentDescription = "close")
            }
            Button(
                onClick = {createEventHandler()},
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                ),
            ) {
                Text(text = "Save", color = MaterialTheme.colorScheme.background,)
            }
        }
        Column(Modifier.fillMaxWidth().fillMaxHeight(.95f).verticalScroll(scrollState, enabled = true).padding(bottom = 10.dp, end = 15.dp, start = 15.dp)) {
            OutlinedTextField(
                value = uiState.eventName,
                onValueChange = {
                    viewModel.onEvent(
                        event=NewEventPageEvent.UpdateEventPageValue(field=NewEventPageState::eventName,it)
                    )
                },
                placeholder = { Text("Title", textAlign = TextAlign.Center) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Green,
                    unfocusedBorderColor = Color.Gray
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Unspecified,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Unspecified,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = {

                    }
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(40.dp),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        modifier = Modifier.clickable {
                            viewModel.onEvent(
                                event=NewEventPageEvent.UpdateEventPageValue(field=NewEventPageState::eventName,"")
                            )
                        },
                        contentDescription = null
                    )
                },
            )
            LazyRow(Modifier.fillMaxWidth().horizontalScroll(rowScrollState).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                items(EventCategory.entries){
                    InputChip(
                        modifier = Modifier.padding(horizontal = 5.dp),
                        label = {
                            Text(text=it.name)
                        },
                        onClick = {},
                        shape = RoundedCornerShape(20.dp),
                        selected = true
                    )
                }
            }
            HorizontalDivider()
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("All day")
                Switch(
                    checked = uiState.isAllDay,
                    onCheckedChange = {
                        viewModel.onEvent(
                            event=NewEventPageEvent.UpdateEventPageValue(
                                field=NewEventPageState::isAllDay,
                                value= it
                            )
                        )
                    }
                )
            }
            Spacer(Modifier.height(16.dp))
            Column(Modifier.fillMaxWidth()) {
                Text("Select Dates")
                Spacer(Modifier.height(16.dp))
                MultiDatePicker(
                    selectedDates = uiState.selectedDates,
                    onDatesSelected = {
                        viewModel.onEvent(
                            event=NewEventPageEvent.UpdateEventPageValue(
                                field=NewEventPageState::selectedDates,
                                value= it
                            )
                        )
                    },
                    maxSelectableDates = 5,
                    minDate = LocalDate.now()
                )
            }
            HorizontalDivider()
            if(!uiState.isAllDay && (uiState.selectedDates.isEmpty() || uiState.selectedDates.size==1) ){
                if (uiState.selectedRecurringType== EventRecurringType.SPECIFIC_DAYS_WEEKLY){
                    Text(text="Your Event will occur every ${uiState.selectedDates.map { it.dayOfWeek.toString() }}")
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Repeat")
                    ExposedDropdownMenuExample(
                        options = EventRecurringType.entries,
                        value= uiState.selectedRecurringType,
                        setValue = {
                            viewModel.onEvent(
                                event=NewEventPageEvent.UpdateEventPageValue(
                                    field=NewEventPageState::selectedRecurringType,
                                    value=it
                                )
                            )
                        }
                    )
                }
                HorizontalDivider()
            }
            Row(
                Modifier.fillMaxWidth().padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Priority")
                ExposedDropdownMenuExample(
                    options = Priority.entries,
                    value= uiState.selectedPriority,
                    setValue = {
                        viewModel.onEvent(
                            event=NewEventPageEvent.UpdateEventPageValue(
                                field=NewEventPageState::selectedPriority,
                                value= it
                            )
                        )
                    }
                )
            }
            HorizontalDivider()
            Row(
                Modifier.fillMaxWidth().padding(all= 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Start Time")
                Text(
                    text= formatTimeFromMillis(timeInMillis=uiState.startTime?.toSecondOfDay()?.toLong() as Long),
                    Modifier.clickable{category="startTime";showTimeDialog=true}
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(all=10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("End Time")
                Text(
                    text=formatTimeFromMillis(uiState.endTime?.toSecondOfDay()?.toLong() as Long),
                    Modifier.clickable{category="endTime";showTimeDialog=true}
                )
            }
            HorizontalDivider()
            Column(Modifier.padding(vertical=10.dp)){
                Text("Location/Link")
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.locationLink,
                    onValueChange = {
                        viewModel.onEvent(
                            event=NewEventPageEvent.UpdateEventPageValue(
                                field=NewEventPageState::locationLink,
                                value= it
                            )
                        )
                    },
                    placeholder = { Text(text="Enter event name", textAlign = TextAlign.Center) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Green,
                        unfocusedBorderColor = Color.Gray
                    ),
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        imeAction = ImeAction.Go
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(40.dp),
                )
            }
            HorizontalDivider()

            Column(Modifier.padding(vertical=10.dp)) {
                Text("Add brief description/Notes")
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = {
                        viewModel.onEvent(
                            event=NewEventPageEvent.UpdateEventPageValue(
                                field=NewEventPageState::description,
                                value= it
                            )
                        )
                    },
                    placeholder = { Text("Write your notes...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 300.dp),
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
fun <T> ExposedDropdownMenuExample(
    value: T,
    setValue: (T) -> Unit,
    options: List<T>,
    label: String = "Select an Option",
    displayText: (T) -> String = { it.toString() } // Converts any type to display text
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.onBackground)
    ) {
        TextField(
            value = displayText(value),
            onValueChange = { /* readOnly → ignore input */ },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
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