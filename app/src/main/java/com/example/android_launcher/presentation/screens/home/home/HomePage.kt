package com.example.android_launcher.presentation.screens.home.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import java.util.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_launcher.R
import com.example.android_launcher.presentation.components.AppItem
import com.example.android_launcher.presentation.components.CalendarItem
import com.example.android_launcher.presentation.screens.home.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import coil3.compose.AsyncImage
import com.example.android_launcher.CAMERA_APP_PACKAGE
import com.example.android_launcher.CLOCK_APP_PACKAGE
import com.example.android_launcher.IS_LOGGED_IN_KEY
import com.example.android_launcher.PHONE_APP_PACKAGE
import com.example.android_launcher.dataStore
import com.example.android_launcher.domain.models.Event
import com.example.android_launcher.presentation.screens.home.home.calendar.CalendarViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date


@Composable()
fun HomePage(viewModel: SharedViewModel = koinViewModel(),calendarViewModel: CalendarViewModel = koinViewModel(), navigateToSettingsPage: ()-> Unit){

    val pinnedApps = viewModel.pinnedApps.collectAsState().value
    val context = LocalContext.current
    val pm = context.packageManager
    val sharedRef = context.getSharedPreferences("settings_value", Context.MODE_PRIVATE)
    val timePattern = remember { mutableStateOf("") }
    var currentTime by remember { mutableStateOf("") }
    var todayDate by remember { mutableStateOf("") }
    var currentHour by remember { mutableStateOf("") }

    val locale = Locale.getDefault()
    val scope = rememberCoroutineScope()
    val formatter = SimpleDateFormat("hh:00 a", Locale.getDefault())

    LaunchedEffect(Unit) {
        val tmFmt = sharedRef.getString("TIME_FORMAT", "24hr")
        timePattern.value = if(tmFmt.toString()=="12hr"){
            "hh:mm a"
        }else{
            "HH:mm"
        }
        while (true){
            val calendar = Calendar.getInstance()
            val formattedTime = formatter.format(calendar.time)
            if (currentHour != formattedTime){
                currentHour = formattedTime
            }
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val dateFmt = SimpleDateFormat("EEEE", locale)
            val monthFmt = SimpleDateFormat("MMMM", locale)
            val daySfx = getDaySuffix(dayOfMonth)

            todayDate = "${dateFmt.format(calendar.time)}, ${dayOfMonth}$daySfx ${monthFmt.format(calendar.time)}"
            val timeFmt = SimpleDateFormat(timePattern.value, locale)
            currentTime = timeFmt.format(calendar.time)
            delay(1000L)
        }
    }
    val timeParts = currentTime.split(" ")
    fun openAlarmSettings(){
        scope.launch {
            val clockApp=context.dataStore.data.map { st ->
                st[CLOCK_APP_PACKAGE]
            }.first()
            if (clockApp!=null){
                val intent = pm?.getLaunchIntentForPackage(clockApp)
                if (intent != null) {
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Can't Launch this app", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context, "No camera app found", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val batteryInfo = viewModel.batteryInfo.collectAsState().value

    val user = Firebase.auth.currentUser
    val todayEvents = calendarViewModel.todayEvents.collectAsState().value
    val now = LocalDateTime.now()
    var hourEvents by remember{
        mutableStateOf<List<Event>>(emptyList())
    }
    LaunchedEffect(todayEvents, currentHour) {
        val nowCalendar = Calendar.getInstance()
        hourEvents = todayEvents.filter { ev ->
            val eventCalendar = Calendar.getInstance().apply {
                timeInMillis = ev.startTime as Long
            }
            eventCalendar.get(Calendar.YEAR) == nowCalendar.get(Calendar.YEAR) &&
                    eventCalendar.get(Calendar.DAY_OF_YEAR) == nowCalendar.get(Calendar.DAY_OF_YEAR) &&
                    eventCalendar.get(Calendar.HOUR_OF_DAY) == nowCalendar.get(Calendar.HOUR_OF_DAY)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 20.dp, bottom = 0.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp, end = 20.dp, start = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Home", fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (user !=null) {
                            OutlinedIconButton(
                                modifier = Modifier.padding(0.dp),
                                onClick = {
                                    navigateToSettingsPage()
                                },
                                content = {
                                    if(user.photoUrl!=null){
                                        AsyncImage(
                                            model = user.photoUrl,
                                            contentDescription = "User profile photo",
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .border(width = 1.dp, color = Color.Transparent, shape = CircleShape)
                                        )
                                    }else{
                                        Box(modifier = Modifier.size(20.dp).background(Color(0xff494949)), contentAlignment = Alignment.Center){
                                            Text(user.displayName.toString().take(2),color=Color.White)
                                        }
                                    }
                                }
                            )
                        }
                        OutlinedIconButton(
                            modifier = Modifier.padding(0.dp),
                            onClick = {
                                navigateToSettingsPage()
                            },
                            content={
                                Icon(
                                    imageVector=Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        )
                    }
                }
                Box(modifier = Modifier.height(250.dp).padding(top = 30.dp)) {
                    Column(
                        Modifier.align(Alignment.Center).fillMaxSize().clickable{openAlarmSettings()},
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text=buildAnnotatedString {
                                append(timeParts.getOrNull(index=0) ?: "")
                                append("")
                                val amPm = timeParts.getOrNull(1)
                                if (!amPm.isNullOrBlank()) {
                                    withStyle(style = SpanStyle(fontSize = 50.sp)) {
                                        append(amPm)
                                    }
                                }

                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 80.sp
                        )
                        Text(todayDate, fontWeight = FontWeight.Light, fontSize = 15.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (batteryInfo?.isCharging==true){
                                Text("Charging")
                            }
                            Text("${batteryInfo?.batteryLevel?:0}%")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.width(20.dp).height(12.dp)
                                    .border(
                                        width=1.dp,
                                        shape = RoundedCornerShape(2.dp),
                                        color =  MaterialTheme.colorScheme.onBackground
                                    )
                                ) {
//                                    if (batteryInfo?.isCharging==true && batteryInfo.batteryLevel < 20) {
//                                        Image(
//                                            painter = painterResource(id = R.drawable.flash),
//                                            contentDescription = "flash charging",
//                                            modifier = Modifier
//                                                .fillMaxHeight()
//                                                .fillMaxWidth(.95f)
//                                                .rotate(90f)
//                                        )
//                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(
                                                (batteryInfo?.batteryLevel?.toFloat())?.div(100.00)
                                                    ?.toFloat() ?: 0f
                                            )
                                            .background(
                                                shape = RoundedCornerShape(2.dp),
                                                color = if(batteryInfo?.isCharging==true) Color(0xff142CFF) else if (isSystemInDarkTheme()) Color.White else Color(
                                                    0xffFFAA6E
                                                )
                                            )
                                    ) {
//                                        if (batteryInfo?.isCharging==true) {
//                                            Image(
//                                                painter = painterResource(id = R.drawable.flash),
//                                                contentDescription = "flash charging",
//                                                modifier = Modifier
//                                                    .fillMaxHeight()
//                                                    .fillMaxWidth(.95f)
//                                                    .rotate(90f)
//                                            )
//                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier.width(1.5.dp).height(height=5.dp)
                                        .background(
                                            shape = RoundedCornerShape(2.dp),
                                            color =MaterialTheme.colorScheme.onBackground
                                        )
                                )
                                if (batteryInfo?.isCharging==true) {
                                    Image(
                                        painter = painterResource(id = R.drawable.flash),
                                        contentDescription = "flash charging",
                                        modifier = Modifier
                                            .height(12.dp)
                                            .padding(horizontal=5.dp)
                                    )
                                }
                            }
                            if (batteryInfo!=null && !batteryInfo.isCharging && batteryInfo.batteryLevel < 20){
                                Text("Battery Low")
                            }
                            if (batteryInfo!=null && batteryInfo.isCharging && batteryInfo.batteryLevel == 100){
                                Text("Battery full, unplug charger.")
                            }
                        }
                    }
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .border(width = .5.dp, color = Color.Gray, shape = RoundedCornerShape(16.dp))
                    .padding(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.calendar),
                                contentDescription = "calendar",
                                modifier = Modifier
                                    .height(20.dp)
                                    .width(20.dp)
                            )
                            Text("Calendar", fontSize = 20.sp, modifier = Modifier.padding(10.dp))
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.ic_down),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    CalendarItem(
                        title = currentHour,
                        events = hourEvents
                    )
                }
                LazyColumn {
                    items(pinnedApps){ap->
                        AppItem(
                            onClick = {
                                viewModel.launchApp(ap)
                            },
                            isInHomeScreen = true,
                            ap = ap,
                            onHideApp = {
                                scope.launch {
                                    viewModel.hideUnhideAppFc(ap,0)
                                }
                            },
                            onUninstallApp = {
                                val intent = Intent(Intent.ACTION_DELETE)
                                intent.data = "package:${ap.packageName}".toUri()
                                context.startActivity(intent)
                            },
                            onBlockApp = {
                                scope.launch {
                                    viewModel.blockUnblockAppFc(ap,1)
                                }
                            },
                            onPinApp = {
                                scope.launch {
                                    viewModel.pinUnpinApp(ap,0)
                                }
                            },
                        )
                    }
                }
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp, end = 10.dp, start = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "No camera app found", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        imageVector=Icons.Default.Phone,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(25.dp).clickable{
                            scope.launch {
                                val phoneApp=context.dataStore.data.map { st ->
                                    st[PHONE_APP_PACKAGE]
                                }.first()
                                if (phoneApp!=null){
                                    val intent = pm?.getLaunchIntentForPackage(phoneApp)
                                    if (intent != null) {
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "Can't Launch this app", Toast.LENGTH_SHORT).show()
                                    }
                                }else{
                                    Toast.makeText(context, "No camera app found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        contentDescription = "Phone"
                    )
                }
                IconButton(onClick = {
                    scope.launch {
                        val cameraApp=context.dataStore.data.map { st ->
                            st[CAMERA_APP_PACKAGE]
                        }.first()
                        if (cameraApp!=null){
                            val intent = pm?.getLaunchIntentForPackage(cameraApp)
                            if (intent != null) {
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "Can't Launch this app", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            Toast.makeText(context, "No camera app found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        modifier = Modifier.size(25.dp),
                        contentDescription = "Camera",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
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