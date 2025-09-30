package com.example.android_launcher.presentation.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_launcher.utils.formatIsoTimeToFriendly
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerItem(modifier: Modifier = Modifier, datePickerHeadLine: @Composable () -> Unit,callBack: (String) -> Unit) {
    val datePickerState = rememberDatePickerState()
    val selectedDateMillis = datePickerState.selectedDateMillis
    var showTimeDialog by remember{mutableStateOf(false)}
    LaunchedEffect(selectedDateMillis){
        Log.d("selected_date","selected date=$selectedDateMillis")
        if (selectedDateMillis != null) {
            showTimeDialog = true // show time picker once a date is picked
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.format(Date(selectedDateMillis))
        }
    }
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
    )
    fun onTimePicked() {
        selectedDateMillis?.let { dateMillis ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = dateMillis
                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                set(Calendar.MINUTE, timePickerState.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }


            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val isoDateTime = isoFormat.format(calendar.time)
            //call the callback function with defined date
            callBack(isoDateTime)
            showTimeDialog = false
        }
    }
    if(showTimeDialog){
        BasicAlertDialog(
            onDismissRequest = {showTimeDialog=false},
            content = {
                Column (horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier=Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)) {
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
    DatePicker(
        state = datePickerState,
        showModeToggle = false,
        headline = {
            datePickerHeadLine()
        },
        title = null,
        colors = DatePickerDefaults.colors(containerColor = Color.Transparent)
    )
}