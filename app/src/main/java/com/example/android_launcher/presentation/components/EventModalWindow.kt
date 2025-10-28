package com.example.android_launcher.presentation.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_launcher.domain.models.Event
import com.example.android_launcher.domain.models.EventCategory
import com.example.android_launcher.presentation.screens.home.home.calendar.NewEventPageEvent
import com.example.android_launcher.presentation.screens.home.home.calendar.NewEventPageState
import com.example.android_launcher.utils.formatTimeToRequiredFormat
import com.example.android_launcher.utils.notifyOptions
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventModalWindow(selectedEvent: Event,onDeletePressed: ()->Unit,onUpdatePressed:()->Unit,updateEventProps:(name: String,value:Any)->Unit, onDismissRequest: () -> Unit,tmFmt: String?="24hr"){
    val sheetState = rememberModalBottomSheetState()
    val scrollState = rememberScrollState()
    var isEditable by remember { mutableStateOf(false) }
    var category by remember{mutableStateOf<String?>(null)}
    val isCheckboxChecked by remember(selectedEvent.endDate) {
        derivedStateOf {
            Log.d("EventModalWindow", "isCheckboxChecked: ${selectedEvent.endDate}")
            val endDate = selectedEvent.endDate
            val parsedDate = runCatching { LocalDate.parse(endDate) }.getOrNull()
            Log.d("EventModalWindow", "isCheckboxChecked: $parsedDate")
            parsedDate != null || parsedDate?.isEqual(LocalDate.now()) == true
        }
    }
    val textColor = when(isEditable){
        true->{
            MaterialTheme.colorScheme.onBackground
        }
        false->{
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        }
    }
    Log.d("EventModalWindow", "isCheckboxChecked: $isCheckboxChecked. endDate=${selectedEvent.endDate}")

    val timePickerState = rememberTimePickerState()
    fun onTimePicked(){
        if (category==null){
            return
        }
        when(category){
            "startTime"->{
                updateEventProps("startTime", LocalTime.of(timePickerState.hour, timePickerState.minute))
            }
            "endTime"->{
                updateEventProps("endTime", LocalTime.of(timePickerState.hour, timePickerState.minute))
            }
        }
        category=null
    }
    if(category!=null){
        BasicAlertDialog(
            onDismissRequest = {
                category=null
            },
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

    ModalBottomSheet( dragHandle = { BottomSheetDefaults.DragHandle() }, onDismissRequest = onDismissRequest,sheetState = sheetState,) {
        Column( modifier = Modifier.verticalScroll(scrollState)){
            Row(Modifier.fillMaxWidth().padding(all=16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Close, contentDescription = "close", Modifier.clickable { onDismissRequest() })
                Text("${selectedEvent.eventCategory.name.replace("_"," ").lowercase().replaceFirstChar { it.uppercase() }} Details")
                Icon(Icons.Default.Edit, contentDescription = "edit", Modifier.clickable { isEditable = !isEditable  })
            }
            TextField(
                modifier=Modifier.padding(vertical=10.dp),
                value=selectedEvent.title,
                onValueChange = {
                    updateEventProps("title",it)
                },
                placeholder = { Text("Event Title") },
                enabled = isEditable,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().padding(all=16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text="Start Time",
                    color = textColor
                )
                Text(
                    text= selectedEvent.startTime.formatTimeToRequiredFormat(tmFmt),
                    Modifier.clickable(enabled = isEditable,onClick={
                        category="startTime"
                        timePickerState.hour = selectedEvent.startTime.hour
                        timePickerState.minute = selectedEvent.startTime.minute
                    }),
                    color = textColor,
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(all=16.dp).clickable(enabled = isEditable,onClick={
                    category="endTime"
                    timePickerState.hour = selectedEvent.endTime.hour
                    timePickerState.minute = selectedEvent.endTime.minute
                }),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text="End Time",
                    color = textColor
                )
                Text(
                    text= selectedEvent.endTime.formatTimeToRequiredFormat(tmFmt),
                    Modifier.clickable{
                        category="endTime"
                        timePickerState.hour = selectedEvent.endTime.hour
                        timePickerState.minute = selectedEvent.endTime.minute
                    },
                    color = textColor,
                )
            }
            HorizontalDivider()
            Column(Modifier.padding(all=16.dp)){
                Row(Modifier.fillMaxWidth()){
                    Icon(
                        imageVector=Icons.Default.Notifications,
                        contentDescription = "notifications",
                        Modifier.padding(end=5.dp),
                        tint = textColor
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            text="Notify ${selectedEvent.notifyBeforeTime} minutes before",
                            color = textColor
                        )
                        LazyRow(verticalAlignment = Alignment.CenterVertically) {
                            items(notifyOptions.keys.toList()){
                                InputChip(
                                    modifier = Modifier.padding(horizontal=5.dp),
                                    label = {
                                        Text(text = notifyOptions[it] ?: "")
                                    },
                                    enabled = isEditable,
                                    selected=false,
                                    onClick = {
                                        if (isEditable){
                                            updateEventProps("notifyBeforeTime",it)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider()
            LazyRow(Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                items(EventCategory.entries){
                    InputChip(
                        enabled = isEditable,
                        modifier = Modifier.padding(horizontal = 5.dp),
                        label = {
                            Text(text=it.name.replace("_"," "))
                        },
                        onClick = {
                            updateEventProps("eventCategory",it)
                        },
                        leadingIcon = if (selectedEvent.eventCategory==it) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = "Done icon",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        },
                        shape = RoundedCornerShape(20.dp),
                        selected = selectedEvent.eventCategory==it
                    )
                }
            }
            HorizontalDivider()
            Column(Modifier.padding(all=16.dp)){
                Text(text="Location/Link",color = textColor)
                Spacer(Modifier.height(16.dp))
                TextField(
                    value = selectedEvent.location.toString(),
                    onValueChange = {
                        updateEventProps("location",it)
                    },
                    enabled = isEditable,
                    placeholder = { Text(text="Location/Link", textAlign = TextAlign.Center) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Go
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            HorizontalDivider()
            Column(Modifier.padding(all=16.dp)) {
                Text(
                    text="Description/Notes",
                    color = textColor
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = selectedEvent.description.toString(),
                    onValueChange = {
                        updateEventProps("description",it)
                    },
                    enabled = isEditable,
                    placeholder = { Text("Write your notes...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 300.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    )
                )
            }
            HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically,) {
                Checkbox(
                    checked = isCheckboxChecked,
                    enabled = isEditable,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.onBackground,

                    ),
                    onCheckedChange = {
                        updateEventProps("endDate", LocalDate.now().toString())
                    }
                )
                Text(text="Mark as completed today",color = textColor)
            }

            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    modifier = Modifier.weight(.8f).heightIn(min = 40.dp),
                    onClick = onDeletePressed,
                    enabled = isEditable,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ),
                ) {
                    Text(text = "Delete", color = textColor)
                }
                Button(
                    modifier = Modifier.weight(.8f).heightIn(min = 40.dp),
                    enabled = isEditable,
                    onClick = onUpdatePressed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        disabledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                    )
                ) {
                    Text(text = "Update", color = textColor)
                }
            }
        }
    }
}