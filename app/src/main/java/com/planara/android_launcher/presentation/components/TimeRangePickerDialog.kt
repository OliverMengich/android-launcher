import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

enum class TimePickerMode {
    START_ONLY,
    RANGE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    mode: TimePickerMode = TimePickerMode.RANGE, // Defaults to selecting both
    onDismissRequest: () -> Unit,
    // Provide defaults for callbacks so you don't have to pass both every time
    onTimePicked: (hour: Int, minute: Int) -> Unit = { _, _ -> },
    onTimeRangePicked: (startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit = { _, _, _, _ -> }
) {
    var isSelectingStartTime by remember { mutableStateOf(true) }
    val calendar = Calendar.getInstance()

    val startTimeState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = false
    )

    val endTimeState = rememberTimePickerState(
        initialHour = (calendar.get(Calendar.HOUR_OF_DAY) + 1) % 24,
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = false
    )

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                // Dynamic Title
                Text(
                    text = if (isSelectingStartTime) "Select Start Time" else "Select End Time",
                    modifier = Modifier.padding(vertical = 8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Switch between the picker views
                if (isSelectingStartTime) {
                    TimePicker(state = startTimeState)
                } else {
                    TimePicker(state = endTimeState)
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Cancel / Back Button
                    TextButton(
                        onClick = {
                            if (!isSelectingStartTime) {
                                isSelectingStartTime = true // Go back to start screen
                            } else {
                                onDismissRequest()
                            }
                        }
                    ) {
                        Text(if (isSelectingStartTime) "Cancel" else "Back")
                    }

                    // Next / OK Button
                    Button(
                        onClick = {
                            if (mode == TimePickerMode.START_ONLY) {
                                // Mode 1: Start time only. Trigger callback and dismiss immediately.
                                onTimePicked(startTimeState.hour, startTimeState.minute)
                            } else {
                                // Mode 2: Range Selection
                                if (isSelectingStartTime) {
                                    isSelectingStartTime = false // Advance to End Time step
                                } else {
                                    // Final step for range
                                    onTimeRangePicked(
                                        startTimeState.hour, startTimeState.minute,
                                        endTimeState.hour, endTimeState.minute
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                        )
                    ) {
                        // Dynamic text: If it's single mode or the final step of range mode, show "OK"
                        val isFinalStep = mode == TimePickerMode.START_ONLY || !isSelectingStartTime
                        Text(
                            text = if (isFinalStep) "OK" else "Next",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.background,
                        )
                    }
                }
            }
        }
    )
}