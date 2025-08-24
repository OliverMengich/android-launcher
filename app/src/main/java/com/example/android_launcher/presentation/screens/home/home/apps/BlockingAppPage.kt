package com.example.android_launcher.presentation.screens.home.home.apps

import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import java.util.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_launcher.utils.formatIsoTimeToFriendly
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockingAppPage(viewModel: BlockingAppViewModel = koinViewModel(), navigateBack:()->Unit,  modifier: Modifier= Modifier, packageName: String){

    var pkgNm by rememberSaveable(packageName) {
        mutableStateOf(packageName)
    }

    LaunchedEffect(key1=pkgNm) {
        viewModel.getAppUsageStats(packageName = pkgNm)
    }
    LaunchedEffect(Unit) {
        viewModel.getAllAppsUsageStats()
    }
    val scope = rememberCoroutineScope()
    var parentWidth by remember { mutableIntStateOf(0) }
    var boxWidth by remember { mutableIntStateOf(0) }
    val offsetX = remember {
        Animatable(initialValue = 0f)
    }
    val maxOffset = (parentWidth -(boxWidth+25)).toFloat().coerceAtLeast(0f)
    var showTimeDialog by remember{mutableStateOf(false)}

    val alpha = animateFloatAsState(
        targetValue =1f- (if (maxOffset > 0f) {
            (offsetX.value / maxOffset).coerceIn(0f, 1f)
        } else 0f),
        animationSpec = tween(durationMillis = 300),
        label = "alpha-animation"
    )
    Log.d("offsetX","offset=${offsetX.value} .alpha=${alpha.value} Parent width=$parentWidth. Box width=$boxWidth")
    val context= LocalContext.current
    val packageManager = context.packageManager
    val appStats = viewModel.appStats.collectAsState().value
    val allAppStats = viewModel.allAppsStats.collectAsState().value

    val datePickerState = rememberDatePickerState()
    val selectedDateMillis = datePickerState.selectedDateMillis

    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
    )

//    var showTimeDialog by remember { mutableStateOf(false) }
    var isoDateTime by remember { mutableStateOf<String?>(null) }

// When user picks a date
    val formattedDate = remember(selectedDateMillis) {
        if (selectedDateMillis != null) {
            showTimeDialog = true // show time picker once a date is picked
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.format(Date(selectedDateMillis))
        } else null
    }

    val selectedDate = datePickerState.selectedDateMillis
    //Block until specific date
    //show all apps usage statistics and sort them by most used to least used.
//    val formattedDate= remember(selectedDate) {
//        showTimeDialog=true
//        selectedDate?.let {
//            Log.d("selected_date","selected date=$selectedDate")
//            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            sdf.format(Date(it))
//        }
//    }

//    val currentTime = Calendar.getInstance()

//    val timePickerState = rememberTimePickerState(
//        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
//        initialMinute = currentTime.get(Calendar.MINUTE),
//    )
    // When user closes the time picker (e.g., with a button):
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
            isoDateTime = isoFormat.format(calendar.time)
            showTimeDialog = false
        }
    }
    fun onDragStopped(){
        scope.launch {
            if (offsetX.value >= maxOffset) {
                Log.d("offset", "drag stopped. has reached end")
                if (isoDateTime !=null){
                    viewModel.blockUnblockAppFc(packageName = pkgNm,blocked=1, blockReleaseDate = isoDateTime)
                    navigateBack()
                    Toast.makeText(context,"${appStats?.name} blocked", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context,"Please select time", Toast.LENGTH_SHORT).show()
                    offsetX.animateTo(targetValue = 0f)
                }
            } else {
                offsetX.animateTo(targetValue = 0f)
                Log.d("offset", "drag stopped. has not reached end.")
            }
        }
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

    LazyColumn(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 10.dp, start = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                }
                Text(text = "Block ${appStats?.name}", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                Text(text = "", textAlign = TextAlign.Center)
            }
            HorizontalDivider()
        }
//        Text(appStats?.name ?: "", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
//        Text(appStats?.difference.toString(), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)

        item {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "You have spent ${appStats?.timeFormat?:"0 secs"} on ${appStats?.name} in the last 7 days. If you want to break but you don't want to uninstall this app, consider blocking to start your detox journey.",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
        item{
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                headline = {
                    Box(Modifier.fillMaxWidth(),contentAlignment= Alignment.Center) {
                        Text(
                            text = "Block ${appStats?.name} until ${formatIsoTimeToFriendly(input = isoDateTime)} ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                title = null,
                colors = DatePickerDefaults.colors(containerColor = Color.Transparent)
            )
        }
        item{
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth(.9f)
                    .border(width = 1.dp,color= MaterialTheme.colorScheme.onBackground, shape = RoundedCornerShape(30.dp))
                    .onSizeChanged {
                        parentWidth = it.width
                    }
            ){

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(size=50.dp).padding(5.dp)
                        .offset { IntOffset(x=offsetX.value.roundToInt(), y=0) }
                        .onSizeChanged {
                            boxWidth = it.width
                        }
                        .background(
                            color=MaterialTheme.colorScheme.onBackground,
                            shape = RoundedCornerShape(50.dp)
                        )
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                scope.launch {
                                    val newOffset = offsetX.value + delta
                                    offsetX.snapTo(newOffset.coerceIn(0f, maxOffset))
                                }
//                                offsetX = newOffset.coerceIn(0f, maxOffset)
                                Log.d("offset","maxOffset=$maxOffset. OffsetX=${offsetX.value}")
//                                if (!hasTriggered && offsetX >= maxOffset) {
//                                    hasTriggered = true
//                                    Log.d("offset","has reached end")
//                                } else if (offsetX < maxOffset) {
//                                    // Reset the flag if user drags back
//                                    hasTriggered = false
//                                }
                            },
                            onDragStopped = {
                                onDragStopped()
                            }
                        )
                        .border(
                            shape = CircleShape,
                            border = BorderStroke(width = 2.dp,  color = MaterialTheme.colorScheme.onBackground)
                        ).align(Alignment.CenterStart),
                ){
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Forward"
                    )
                }
                Text(text="SLIDE TO BLOCK",modifier = Modifier.alpha(alpha.value).align(Alignment.Center), fontWeight = FontWeight.Bold,  textAlign = TextAlign.Center)
            }
        }
        item {
            Box(Modifier.fillMaxWidth().padding(vertical = 20.dp),contentAlignment= Alignment.Center) {
                Text(
                    text = "Other Applications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
        items(items = allAppStats){ stat->
            Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text=stat.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(text="Used ${stat.timeFormat.toString()}", fontSize = 10.sp)
                }
                TextButton(
                    onClick = {
                        pkgNm = stat.packageName
                    },
                    content = {
                        Text(text="Block",color= MaterialTheme.colorScheme.onBackground)
                    }
                )
            }
            HorizontalDivider()
        }
    }
}
