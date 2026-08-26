package com.planara.android_launcher.presentation.screens.home.home.focus

import TimePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planara.android_launcher.domain.models.FocusMode
import com.planara.android_launcher.domain.models.FocusType
import com.planara.android_launcher.domain.models.UsageTime
import com.planara.android_launcher.presentation.components.DateTimePickerItem
import com.planara.android_launcher.presentation.components.FocusCountdown
import com.planara.android_launcher.presentation.components.remainingMillisUntil
import com.planara.android_launcher.presentation.screens.home.settings.AccordionItem
import com.planara.android_launcher.utils.currentWindow
import com.planara.android_launcher.utils.currentWindowEnd
import com.planara.android_launcher.utils.describe
import com.planara.android_launcher.utils.formatIsoTimeToFriendly
import com.planara.android_launcher.utils.formatTimeToRequiredFormat
import com.planara.android_launcher.utils.isActive
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

/**
 * State for creating a new focus session, analogous to
 * [com.planara.android_launcher.presentation.screens.home.home.apps.BlockState].
 * Existing sessions are listed read-only and are never edited.
 */
data class FocusState(
    val focusType: FocusType = FocusType.NORMAL,
    val startDate: String = "",
    val endDate: String = "",
    val usageTimes: List<UsageTime> = emptyList()
) {
    fun toFocusMode(): FocusMode = FocusMode(
        startDate = startDate,
        endDate = endDate,
        focusType = focusType,
        usageTimes = if (focusType == FocusType.NORMAL) emptyList() else usageTimes
    )
}

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModePage(
    viewModel: FocusModeViewModel = koinViewModel(),
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localManagerData = viewModel.localManagerData.collectAsState().value

    // All enabled sessions (active or scheduled). Several can coexist.
    val focusModes = remember(localManagerData.focusMode, localManagerData.focusModes) {
        localManagerData.activeFocusModes()
    }

    // Tick every second so the countdown is live.
    val nowTick by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            delay(1.seconds)
            value = System.currentTimeMillis()
        }
    }
    val now = remember(nowTick) { LocalDateTime.now() }
    val activeFocusModes = remember(focusModes, nowTick) { focusModes.filter { it.isActive(now) } }
    val activeCount = activeFocusModes.size
    // The session driving the countdown: the one with the nearest end.
    val countdownTarget = remember(activeFocusModes, nowTick) {
        activeFocusModes
            .mapNotNull { mode -> mode.currentWindowEnd(now)?.let { mode to it } }
            .minByOrNull { it.second }
    }
    // Same format the home clock uses.
    val timeFormat = localManagerData.displaySettings.timeFormat

    var editing by rememberSaveable { mutableStateOf<FocusState?>(null) }

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp, start = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = navigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
            }
            Text(
                text = "Focus Mode",
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(text = "", textAlign = TextAlign.Center)
        }
        HorizontalDivider()

        if (editing == null) {
            FocusSessionsOverview(
                focusModes = focusModes,
                activeCount = activeCount,
                countdownTarget = countdownTarget,
                now = now,
                timeFormat = timeFormat,
                onAdd = { editing = FocusState() }
            )
        } else {
            FocusSessionEditor(
                state = editing!!,
                onStateChange = { editing = it },
                onSave = { state ->
                    scope.launch {
                        val error = viewModel.saveFocusMode(state.toFocusMode())
                        if (error == null) {
                            editing = null
                            Toast.makeText(context, "Focus session enabled", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onCancel = { editing = null }
            )
        }
    }
}

@Composable
private fun FocusSessionsOverview(
    focusModes: List<FocusMode>,
    activeCount: Int,
    countdownTarget: Pair<FocusMode, LocalDateTime>?,
    now: LocalDateTime,
    timeFormat: String,
    onAdd: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "Focus Mode helps you take control of your time by minimizing distractions and limiting phone use while you work, study, or rest. " +
                            "When Focus Mode is on, your phone stays locked for everything except essential phone calls. " +
                            "You can have several sessions at once, e.g. a long focus until a date plus a short focus today from 10am to 1pm.",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
            HorizontalDivider()
        }

        // Live countdown for the running session that ends first.
        countdownTarget?.let { (mode, windowEnd) ->
            val start = mode.currentWindow(now)
            val startOfWindow = start?.startTime
                ?.let { runCatching { now.toLocalDate().atTime(LocalTime.parse(it)) }.getOrNull() }
            // Full length of the window, so the ring depletes uniformly.
            val totalMillis = startOfWindow
                ?.let { Duration.between(it, windowEnd).toMillis() }
                ?.coerceAtLeast(1L)
                ?: Duration.between(now, windowEnd).toMillis().coerceAtLeast(1L)
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FocusCountdown(
                        remainingMillis = remainingMillisUntil(windowEnd, now),
                        totalMillis = totalMillis,
                        startTime = startOfWindow?.toLocalTime(),
                        endTime = windowEnd.toLocalTime(),
                        timeFormat = timeFormat,
                        ringSize = 220.dp,
                        valueFontSize = 34
                    )
                }
                HorizontalDivider()
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 18.dp, top = 15.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Focus Sessions", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(
                        text = if (focusModes.isEmpty()) "None enabled" else "$activeCount active · ${focusModes.size - activeCount} upcoming",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground)
                ) {
                    Icon(Icons.Default.Add, null)
                    Text("Add", color = MaterialTheme.colorScheme.background)
                }
            }
        }

        if (focusModes.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No focus sessions yet.\nTap Add to schedule your first one.",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            items(focusModes, key = { it.id }) { mode ->
                val active = mode.isActive()
                Column(
                    modifier = Modifier
                        .fillMaxWidth(.94f)
                        .padding(vertical = 5.dp)
                        .border(
                            width = 1.dp,
                            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = if (active) "Active now" else "Scheduled",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Text(
                        text = mode.describe(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    // Sessions are listed read-only: running ones end on their end date and
                    // scheduled ones can't be edited or cancelled.
                    Text(
                        text = if (active) {
                            "Locked until it ends. Focus Mode can't be turned off early."
                        } else {
                            "Scheduled. Focus Mode will start automatically."
                        },
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FocusSessionEditor(
    state: FocusState,
    onStateChange: (FocusState) -> Unit,
    onSave: (FocusState) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showTimeDialog by remember { mutableStateOf(false) }

    var parentWidth by remember { mutableIntStateOf(0) }
    var boxWidth by remember { mutableIntStateOf(0) }
    val offsetX = remember { Animatable(initialValue = 0f) }
    val maxOffset = (parentWidth - (boxWidth + 25)).toFloat().coerceAtLeast(0f)
    val alpha = animateFloatAsState(
        targetValue = 1f - (if (maxOffset > 0f) {
            (offsetX.value / maxOffset).coerceIn(0f, 1f)
        } else 0f),
        animationSpec = tween(durationMillis = 300),
        label = "focus-alpha-animation"
    )

    if (showTimeDialog) {
        TimePickerDialog(
            onDismissRequest = { showTimeDialog = false },
            onTimeRangePicked = { startH, startM, endH, endM ->
                val start = LocalTime.of(startH, startM)
                val end = LocalTime.of(endH, endM)
                onStateChange(
                    state.copy(
                        usageTimes = state.usageTimes + UsageTime(
                            startTime = start.toString(),
                            endTime = end.toString()
                        )
                    )
                )
                showTimeDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Session ends ${if (state.endDate.isBlank()) "at a selected time" else formatIsoTimeToFriendly(input = state.endDate)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)
            )
            DateTimePickerItem(
                callBack = { isoDate -> onStateChange(state.copy(endDate = isoDate)) },
                datePickerHeadLine = {
                    Text(
                        text = "Select when this session ends",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            )
            if (state.startDate.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Starts ${formatIsoTimeToFriendly(state.startDate)}", fontSize = 14.sp)
                    TextButton(onClick = { onStateChange(state.copy(startDate = "")) }) {
                        Text("Start now")
                    }
                }
            }
            HorizontalDivider()
        }

        item {
            AccordionItem(title = "Focus all day, every day?") {
                Column(Modifier.fillMaxWidth()) {
                    Text("Your phone stays locked for everything except essential phone calls until the selected end date.")
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onStateChange(state.copy(focusType = FocusType.NORMAL, usageTimes = emptyList()))
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground)
                    ) {
                        Text(if (state.focusType == FocusType.NORMAL) "Selected" else "Select")
                    }
                }
            }
        }

        item {
            AccordionItem(title = "Focus at specific times?") {
                Column(Modifier.fillMaxWidth()) {
                    Text("Focus Mode only turns on during the times you select (for example 10:00 AM - 1:00 PM today). This session can run alongside other sessions.")
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            showTimeDialog = true
                            if (state.focusType !== FocusType.FOCUS_BETWEEN) {
                                onStateChange(state.copy(focusType = FocusType.FOCUS_BETWEEN, usageTimes = emptyList()))
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Text("Add time")
                    }
                    if (state.focusType === FocusType.FOCUS_BETWEEN) {
                        UsageTimeList(
                            times = state.usageTimes,
                            onDelete = { time -> onStateChange(state.copy(usageTimes = state.usageTimes.filter { it != time })) }
                        )
                    }
                }
            }
        }

        item {
            AccordionItem(title = "Don't focus at specific times?") {
                Column(Modifier.fillMaxWidth()) {
                    Text("Focus Mode stays on all day except during the times you select (for example 12:00 PM - 1:00 PM for lunch).")
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            showTimeDialog = true
                            if (state.focusType !== FocusType.NO_FOCUS_BETWEEN) {
                                onStateChange(state.copy(focusType = FocusType.NO_FOCUS_BETWEEN, usageTimes = emptyList()))
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Text("Add time")
                    }
                    if (state.focusType === FocusType.NO_FOCUS_BETWEEN) {
                        UsageTimeList(
                            times = state.usageTimes,
                            onDelete = { time -> onStateChange(state.copy(usageTimes = state.usageTimes.filter { it != time })) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(15.dp))
        }

        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth(.9f)
                    .border(1.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(30.dp))
                    .onSizeChanged { parentWidth = it.width }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(size = 50.dp)
                        .padding(5.dp)
                        .offset { IntOffset(x = offsetX.value.roundToInt(), y = 0) }
                        .onSizeChanged { boxWidth = it.width }
                        .background(color = MaterialTheme.colorScheme.onBackground, shape = RoundedCornerShape(50.dp))
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                scope.launch {
                                    offsetX.snapTo((offsetX.value + delta).coerceIn(0f, maxOffset))
                                }
                            },
                            onDragStopped = {
                                scope.launch {
                                    if (offsetX.value >= maxOffset && maxOffset > 0f) {
                                        when {
                                            state.endDate.isBlank() -> {
                                                Toast.makeText(context, "Please select when this session ends", Toast.LENGTH_SHORT).show()
                                                offsetX.animateTo(0f)
                                            }
                                            state.focusType !== FocusType.NORMAL && state.usageTimes.isEmpty() -> {
                                                Toast.makeText(context, "Add at least one time, or choose focus all day", Toast.LENGTH_SHORT).show()
                                                offsetX.animateTo(0f)
                                            }
                                            else -> onSave(state)
                                        }
                                    } else {
                                        offsetX.animateTo(targetValue = 0f)
                                    }
                                }
                            }
                        )
                        .border(shape = CircleShape, border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.onBackground))
                        .align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Forward"
                    )
                }
                Text(
                    text = "SLIDE TO ENABLE",
                    modifier = Modifier.alpha(alpha.value).align(Alignment.Center),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onCancel) { Text("Cancel", color = MaterialTheme.colorScheme.onBackground) }
            }
        }
    }
}

@Composable
private fun UsageTimeList(times: List<UsageTime>, onDelete: (UsageTime) -> Unit) {
    if (times.isEmpty()) return
    Spacer(Modifier.height(10.dp))
    times.forEach { time ->
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val start = runCatching { LocalTime.parse(time.startTime).formatTimeToRequiredFormat("hh:mm a") }.getOrDefault(time.startTime)
            val end = runCatching { LocalTime.parse(time.endTime).formatTimeToRequiredFormat("hh:mm a") }.getOrDefault(time.endTime)
            Text(text = "$start - $end")
            IconButton(onClick = { onDelete(time) }, content = { Icon(Icons.Default.Delete, null) })
        }
        HorizontalDivider()
    }
}
