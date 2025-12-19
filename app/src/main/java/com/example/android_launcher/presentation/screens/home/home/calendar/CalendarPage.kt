package com.example.android_launcher.presentation.screens.home.home.calendar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android_launcher.R
import com.example.android_launcher.domain.models.Event
import com.example.android_launcher.domain.models.EventCategory
import com.example.android_launcher.domain.models.EventRecurringType
import com.example.android_launcher.domain.models.Priority
import com.example.android_launcher.presentation.components.EventModalWindow
import com.example.android_launcher.presentation.screens.home.SharedViewModel
import com.example.android_launcher.utils.formatGivenDateToRequiredFormat
import com.example.android_launcher.utils.formatHourPattern
import com.example.android_launcher.utils.formatLocalDateToRequiredFormat
import com.example.android_launcher.utils.formatTimeToRequiredFormat
import com.example.android_launcher.utils.priorityColors
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class EventsOrder(
    val id: Int,
    val title: String,
    val events: List<Event>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarPage(
    viewModel: CalendarViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinViewModel(),
    navigateToHome:()->Unit,
    navigateToNewEvent:()->Unit
){
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val listState = rememberLazyListState()
    var startDate by remember {
        mutableStateOf<LocalDate>(LocalDate.now())
    }
    val context= LocalContext.current
    val localManagerData by sharedViewModel.localManagerData.collectAsStateWithLifecycle()
    val tmFmt by remember(localManagerData) {
        val strF = localManagerData.displaySettings.timeFormat.replace(Regex(":(mm|ss)(:ss)?"), ":00")
        mutableStateOf(strF)
    }
    BackHandler {
        navigateToHome()
    }
    LaunchedEffect(key1=Unit, key2=startDate) {
        viewModel.generateDays(dt=startDate)
        viewModel.getDateEvents(startDate)
    }

    var selectedEvent by remember{
        mutableStateOf<Event?>(null)
    }
    val weeks = viewModel.dates.collectAsState().value
    val dateEvents by viewModel.dateEvents.collectAsStateWithLifecycle()
    val eventsList by remember(dateEvents) {
        derivedStateOf {
            dateEvents
                .groupBy { ev -> ev.startTime.hour }
                .map { (hourId, eventsInHour) ->
                    val firstEventTime = eventsInHour.first().startTime
                    val hourValue = formatHourPattern(tmFmt,firstEventTime.hour)
                    EventsOrder(id = hourId, title = hourValue, events = eventsInHour)
                }
                .sortedBy { it.id }
        }
    }

    val categoryCounts by remember(key1=dateEvents) {
        derivedStateOf {
            dateEvents.groupingBy { it.eventCategory }.eachCount()
        }
    }

    Column(modifier = Modifier.fillMaxSize())  {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 20.dp, bottom = 0.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, end = 20.dp, start = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text="Calendar", fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedIconButton(
                        modifier = Modifier.padding(0.dp),
                        onClick = navigateToNewEvent,
                        content = {
                            Icon(
                                imageVector=Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    )
                    OutlinedIconButton(
                        modifier = Modifier.padding(0.dp),
                        onClick = {
                            showDatePicker = true
                        },
                        content = {
                            Image(
                                painter = painterResource(id = R.drawable.calendar),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    )
                }
            }
        }
        Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp, end = 15.dp, start = 15.dp), horizontalAlignment = Alignment.Start) {
            Text(
                text= categoryCounts.entries.joinToString(", ") {(category,count)->
                    "$count ${category.name.lowercase().replaceFirstChar { it.titlecase() }}"
                },
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
            Text(text = formatGivenDateToRequiredFormat(givenDate = startDate, dateFormat = localManagerData.displaySettings.dateFormat), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            WeekPager(
                Modifier.padding(vertical = 10.dp),
                weeks = weeks,
                startDate = startDate,
                onDateItemClick = { dt ->
                    startDate=dt
                }
            )
            if (eventsList.isEmpty()){
                Text(text="No events for this day, create one",color= MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center,modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp))
            }
            LazyColumn(Modifier.fillMaxWidth(),state = listState) {
                items(eventsList){e->
                    Row(Modifier.padding(horizontal=4.dp)){
                        Text(text=e.title)
                        Column(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
                                Box(modifier = Modifier.width(20.dp).height(20.dp).border(shape = CircleShape, border = BorderStroke(2.dp, color= MaterialTheme.colorScheme.onBackground))){}
                                HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 2.dp,)
                            }
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.heightIn(max = 500.dp).fillMaxWidth().padding(vertical=10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(e.events){item->
                                    Column(Modifier.background(color= priorityColors[item.priority]!!, shape = RoundedCornerShape(10.dp)).padding(horizontal = 5.dp, vertical = 5.dp).clickable{selectedEvent=item}){
                                        Text(text=item.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        localManagerData.displaySettings.timeFormat.let { timeFormatItem->
                                            Text(
                                                text="${item.startTime.formatTimeToRequiredFormat(pattern = timeFormatItem)} - ${item.endTime.formatTimeToRequiredFormat(pattern = timeFormatItem)} "
                                            )
                                        }
                                        Text(text=item.location.toString(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedEvent !=null) {
            EventModalWindow(
                onDismissRequest = { selectedEvent=null },
                selectedEvent=selectedEvent!!,
                updateEventProps = { nm,vl->
                    when(nm){
                        "title"->selectedEvent=selectedEvent!!.copy(title = vl as String)
                        "location"->selectedEvent=selectedEvent!!.copy(location = vl as String)
                        "description"->selectedEvent=selectedEvent!!.copy(description = vl as String)
                        "eventCategory"->selectedEvent=selectedEvent!!.copy(eventCategory = vl as EventCategory)
                        "startTime"->selectedEvent=selectedEvent!!.copy(startTime = vl as LocalTime)
                        "endTime"->selectedEvent=selectedEvent!!.copy(endTime = vl as LocalTime)
                        "endDate"->selectedEvent=selectedEvent!!.copy(endDate = vl as String)
                        "isAllDay"->selectedEvent=selectedEvent!!.copy(recurringType = vl as EventRecurringType)
                        "dates" -> selectedEvent=selectedEvent!!.copy(dates = (vl as Set<LocalDate>).map { it.toString() }.toList())
                        "selectedPriority" -> selectedEvent=selectedEvent!!.copy(priority = vl as Priority)
                        "notifyBeforeTime" -> selectedEvent=selectedEvent!!.copy(notifyBeforeTime = vl as Int)
                        "selectedRecurringType" -> selectedEvent=selectedEvent!!.copy(recurringType = vl as EventRecurringType)
                    }
                },
                tmFmt = tmFmt,
                onDeletePressed = {
                    viewModel.deleteEvent(selectedEvent!!.id)
                    viewModel.getDateEvents(startDate)
                    selectedEvent=null
                },
                onUpdatePressed = {
                    viewModel.updateEvent(selectedEvent!!)
                    viewModel.getDateEvents(startDate)
                    selectedEvent=null
                }
            )
        }
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false  },
                confirmButton = {
                    TextButton(onClick = {
                        val selectedDate = datePickerState.selectedDateMillis
                        val localDt = longToLocalDate(selectedDate)
                        startDate = localDt
                        showDatePicker  = !showDatePicker
                    }) {
                        Text("OK")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}

@Composable
fun WeekPager(modifier: Modifier = Modifier, weeks: List<List<LocalDate>>,startDate: LocalDate,onDateItemClick:(LocalDate)->Unit) {
    val calculatedPage = findPageIndexContainingDate(weeks, startDate).coerceAtLeast(0)
    val pagerState = rememberPagerState(pageCount = { weeks.size }, )

    LaunchedEffect(calculatedPage) {
        pagerState.scrollToPage(calculatedPage)
    }
    val dayFormatter = DateTimeFormatter.ofPattern("E") // Mon, Tue, etc.
    val dateFormatter = DateTimeFormatter.ofPattern("dd") // e.g. 13 Jun 2025
    HorizontalPager(state = pagerState,  modifier = modifier.padding(top=20.dp)) { page ->
        val week = weeks[page]
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            week.forEach { date ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.height(height=80.dp).clickable{onDateItemClick(date)}.background(color= if(date==startDate) Color.Red else Color.Transparent, shape = RoundedCornerShape(20.dp)).padding(horizontal = 6.dp)) {
                    Text(text = date.format(dayFormatter), fontSize = 18.sp, color=if(date==startDate) Color.White else MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                    Text(text = date.format(dateFormatter), fontSize = 14.sp, color=if(date== startDate) Color.White else MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}
fun findPageIndexContainingDate(weeks: List<List<LocalDate>>, targetDate: LocalDate): Int {
    return weeks.indexOfFirst { week -> targetDate in week }
}
fun longToLocalDate(epochMilli: Long?): LocalDate {
    if (epochMilli==null){
        return LocalDate.now()
    }
    return Instant.ofEpochMilli(epochMilli)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}
