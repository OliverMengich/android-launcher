package com.planara.android_launcher.presentation.components

import androidx.compose.ui.graphics.Color
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.planara.android_launcher.R
import com.planara.android_launcher.domain.models.DeviceCalendar

enum class PermissionStep{
    PERMISSION,
    CALENDAR
}

@Composable
fun DialogCalendarModule(
    requestPermission: () -> Unit,
    requestCalendar: () -> Unit,
    onClose: ()-> Unit,
    permissionStep: PermissionStep?= PermissionStep.PERMISSION,
    deviceCalendars: List<DeviceCalendar>,
    onSelectCalendar: (Long) -> Unit,
    onSkipSelectCalendar: ()->Unit,
){
    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        ),
        onDismissRequest = { onClose() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(.85f)
                .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(20.dp))
                .wrapContentHeight()
                .padding(1.dp)
        ) {
            AnimatedContent(
                targetState = permissionStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(size = 20.dp)
                    ),
                label = "Card slide animation",
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                }
            ) { step ->
                when (step) {
                    PermissionStep.PERMISSION -> {
                        DialogCard(
                            title = "Calendar Permission",
                            description = buildAnnotatedString {
                                append("We use calendar access to add and manage your events, prevent scheduling conflicts, and send timely reminders—your data stays private and under your control.")
                            },
                            icon = Icons.Default.CalendarMonth,
                            iconColor = MaterialTheme.colorScheme.onBackground
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Button(
                                    onClick = {
                                        requestPermission()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.background,
                                        disabledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Text(
                                        text = "Grant permission",
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                    PermissionStep.CALENDAR->{
                        LaunchedEffect(Unit) {
                            requestCalendar()
                        }
                        DialogCard(
                            title = "Default Calendar",
                            description = buildAnnotatedString {
                                append("Please select the default calendar to use for events")
                            },
                            icon = Icons.Default.CalendarMonth,
                            iconColor = MaterialTheme.colorScheme.onBackground
                        ) {
                            LazyColumn {
                                items(deviceCalendars){
                                    Text(
                                        text=it.name,
                                        modifier = Modifier
                                            .padding(vertical = 5.dp)
                                            .clickable{
                                                onSelectCalendar(it.id)
                                            }
                                    )
                                }
                            }
                            TextButton(
                                onClick = onSkipSelectCalendar,
                                modifier = Modifier.align(Alignment.Center).padding(all = 20.dp),
                                content = {
                                    Text(
                                        text="SKIP",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(10.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 15.sp,
                                    )
                                }
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}



@Composable
fun DialogCard(
    title: String,
    description: AnnotatedString,
    iconColor: Color,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    val cardModifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "",
                tint = iconColor,
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = title,
                modifier = Modifier.padding(vertical=16.dp),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 25.sp
                )
            )
            Text(
                text = description,
                modifier = Modifier.padding(vertical=16.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )
            content()
        }
    }
}
