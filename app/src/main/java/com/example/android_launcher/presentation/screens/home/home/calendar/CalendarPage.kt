package com.example.android_launcher.presentation.screens.home.home.calendar

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
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_launcher.R
import com.example.android_launcher.domain.models.Event
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

data class EventsOrder(
    val id: Int,
    val title: String,
    val events: List<Event>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarPage(viewModel: CalendarViewModel = koinViewModel(),navigateToNewEvent:()->Unit,){
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val listState = rememberLazyListState()
    var startDate by remember {
        mutableStateOf<LocalDate>(LocalDate.now())
    }
    LaunchedEffect(Unit, startDate) {
        viewModel.generateDays(dt=startDate)
    }

//    val hourFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    var selectedEvent by remember{
        mutableStateOf<Event?>(null)
    }
    val weeks = viewModel.dates.collectAsState().value
    val hourFormat = remember { SimpleDateFormat("h:00 a", Locale.getDefault()) }
    var eventsList by remember { mutableStateOf<List<EventsOrder>>(emptyList()) }
    val todayEvents = viewModel.todayEvents.collectAsState().value

    LaunchedEffect(todayEvents) {
        eventsList = todayEvents
            .groupBy { ev ->
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(ev.startTime as Long),
                    ZoneId.systemDefault()
                ).hour
            }
            .map { (hourId, eventsInHour) ->
                val firstEventMillis = eventsInHour.first().startTime as Long
                val hourValue = hourFormat.format(Date(firstEventMillis))
                    .lowercase(Locale.getDefault())
                EventsOrder(
                    id = hourId,
                    title = hourValue,
                    events = eventsInHour
                )
            }
            .sortedBy { it.id }
    }
    val pagerState = rememberPagerState(pageCount = { weeks.size })
    val activeDateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
    Column(modifier = Modifier.fillMaxSize())  {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 20.dp, bottom = 0.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, end = 20.dp, start = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Calendar", fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedIconButton(
                        modifier = Modifier.padding(0.dp),
                        onClick = navigateToNewEvent,
                        content = {
                            Icon(
                                Icons.Default.Add,
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
            Text("3 Tasks today, 2 Meetings", fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text(text= activeDateFormatter.format(startDate), fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
            WeekPager(
                Modifier.padding(vertical = 10.dp),
                weeks = weeks,
                startDate=startDate,
                onDateItemClick = { dt->
                    startDate=dt
                }
            )
            if (eventsList.isEmpty()){
                Text(text="No events for this day, create one",color= MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center,modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp))
            }
            LazyColumn(Modifier.fillMaxWidth(),state = listState) {
                items(eventsList){e->
                    Row{
                        Text(text=e.title, Modifier.fillMaxWidth(.17f))
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
                                    Column(Modifier.background(Color.Red, shape = RoundedCornerShape(10.dp)).padding(horizontal = 5.dp, vertical = 5.dp).clickable{selectedEvent=item}){
                                        Text( item.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("09:45am-10:00am")
                                        Text("Skype")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedEvent !=null) {
            ModalBottomSheet(onDismissRequest = { selectedEvent=null },Modifier.heightIn(min=400.dp),sheetState = rememberModalBottomSheetState(),) {
                Text("show bottom sheet.")
            }
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