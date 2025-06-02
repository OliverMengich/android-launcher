package com.example.android_launcher.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import java.util.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar

@Composable()
fun HomePage(){

    var currentTime by remember { mutableStateOf("") }
    var todayDate by remember { mutableStateOf("") }
    val locale = Locale.getDefault()

    LaunchedEffect(Unit) {
        while (true){
            val calendar = Calendar.getInstance()
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val dateFmt = SimpleDateFormat("EEEE", locale)
            val monthFmt = SimpleDateFormat("d MMMM", locale)
            val daySfx = getDaySuffix(dayOfMonth)
            todayDate = "${dateFmt.format(calendar.time)}, ${dayOfMonth}$daySfx ${monthFmt.format(calendar.time)}"
            val timeFmt = SimpleDateFormat("HH:mm",locale)
            currentTime = timeFmt.format(calendar.time)
            delay(60000L)
        }
    }
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.height(200.dp).width(200.dp).padding(top = 50.dp)) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                progress = { 0.5f },
                color = Color.Red,
                strokeWidth = 10.dp,
                trackColor = Color.White,
            )
            Column(Modifier.align(Alignment.Center).fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(currentTime, fontWeight = FontWeight.Bold, fontSize = 30.sp)
                Text(todayDate, fontWeight = FontWeight.Light, fontSize = 20.sp)
                Text("31%")
            }
        }
        Box(contentAlignment = Alignment.BottomStart, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Phone, tint = Color.Red, modifier = Modifier.size(20.dp), contentDescription = "Phone")
            Icon(Icons.Default.Phone,contentDescription = "Phone")
        }
    }
}
private fun getDaySuffix(day: Int): String {
    if (day in 11..13) {
        return "th"
    }
    return when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}