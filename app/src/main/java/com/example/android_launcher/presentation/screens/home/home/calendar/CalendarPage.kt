package com.example.android_launcher.presentation.screens.home.calendar

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BasicAlertDialog
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.android_launcher.R
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarPage(viewModel: CalendarViewModel = koinViewModel()){
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    LaunchedEffect(Unit) {
        viewModel.generateDays()
    }
    val weeks = viewModel.dates.collectAsState().value
    val pagerState = rememberPagerState(pageCount = {
        weeks.size
    })
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
                        onClick = {
                        },
                        content = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (isSystemInDarkTheme()) Color.White else Color.Black
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
            Text(activeDateFormatter.format(LocalDate.now()), fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
            WeekPager(weeks, LocalDate.now())

        }
        HorizontalDivider(modifier = Modifier.width(2.dp).fillMaxWidth(), thickness = 2.dp, color = Color.Black)
        Column(modifier = Modifier.height(200.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.width(20.dp).height(20.dp).border(shape = CircleShape, border = BorderStroke(2.dp, color = Color.Black))){}
            VerticalDivider(color = Color.Black, thickness = 2.dp)
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false  },
                confirmButton = {
                    TextButton(onClick = {
                        val selectedDate = datePickerState.selectedDateMillis
                        // handle confirm
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
fun WeekPager(weeks: List<List<LocalDate>>,startDate: LocalDate) {
    val calculatedPage = findPageIndexContainingDate(weeks, startDate).coerceAtLeast(0)
    val pagerState = rememberPagerState(pageCount = { weeks.size }, )

    LaunchedEffect(calculatedPage) {
        pagerState.scrollToPage(calculatedPage)
    }
    val dayFormatter = DateTimeFormatter.ofPattern("E") // Mon, Tue, etc.
    val dateFormatter = DateTimeFormatter.ofPattern("dd") // e.g. 13 Jun 2025
    HorizontalPager(state = pagerState,  modifier = Modifier.padding(top=20.dp)) { page ->
        val week = weeks[page]
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            week.forEach { date ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.height(90.dp).background(if (date== LocalDate.now())Color.Red else Color.Transparent, shape = RoundedCornerShape(20.dp)).padding(horizontal = 10.dp)) {
                    Text(text = date.format(dayFormatter), fontSize = 20.sp, color = if (date== LocalDate.now()) Color.White else Color(0xff031c1f), fontWeight = FontWeight.Bold)
                    Text(text = date.format(dateFormatter), color = if (date== LocalDate.now()) Color.White else Color(0xff031c1f))
                }
            }
        }
    }
}
fun findPageIndexContainingDate(weeks: List<List<LocalDate>>, targetDate: LocalDate): Int {
    return weeks.indexOfFirst { week -> targetDate in week }
}