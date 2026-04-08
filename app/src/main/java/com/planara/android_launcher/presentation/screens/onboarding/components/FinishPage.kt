package com.planara.android_launcher.presentation.screens.onboarding.components

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planara.android_launcher.dataStore
import com.planara.android_launcher.domain.models.DeviceCalendar
import com.planara.android_launcher.presentation.components.DialogCalendarModule
import com.planara.android_launcher.presentation.components.PermissionStep
import kotlinx.coroutines.launch

@Composable
fun FinishPage(
    isActive: Boolean,
    navigateToNextPage:()-> Unit,
    navigateToPrev: ()->Unit,
    requestDeviceCalendars:()-> Unit,
    deviceCalendars: List<DeviceCalendar>
) {
    val context = LocalContext.current
    var showCalendarDialog by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var permissionStep by remember { mutableStateOf<PermissionStep?>(PermissionStep.PERMISSION) }

     //1️⃣ Create a permission launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val isGranted = permissions.values.all { it }
            if (isGranted) {
                Toast.makeText(context, "Calendar permission granted", Toast.LENGTH_SHORT).show()
                permissionStep = PermissionStep.CALENDAR
//                navigateToNextPage()
            } else {
                Toast.makeText(context, "calendar permission denied", Toast.LENGTH_SHORT).show()
                // You can still navigate or show a dialog explaining why it's needed
//                navigateToNextPage()
            }
        }
    )
    if (showCalendarDialog){
        DialogCalendarModule(
            requestPermission = {
                permissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR
                    )
                )
            },
            requestCalendar = requestDeviceCalendars,
            onClose = {
                showCalendarDialog = false
                permissionStep = null
            },
            permissionStep = permissionStep,
            deviceCalendars = deviceCalendars,
            onSkipSelectCalendar = {
                permissionStep = null
                showCalendarDialog = false
            },
            onSelectCalendar = { cal->
                scope.launch {
                    context.dataStore.updateData {
                        it.copy(
                            selectedDeviceCalendar = cal
                        )
                    }
                    permissionStep = null
                    showCalendarDialog = false
                }
            }
        )
    }
    Box (modifier = Modifier.fillMaxSize()){
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "All set",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

//            Text(
//                text = "You're all set! Before we begin, please enable notifications so you never miss important events and to keep Focus Mode running smoothly.",
//                style = MaterialTheme.typography.bodyLarge,
//                textAlign = TextAlign.Center,
//                color = MaterialTheme.colorScheme.onBackground
//            )
            Text(
                text = "You're all set! Your launcher is now configured to keep your focus intact and your schedule within reach. Every element has been designed with simplicity in mind—less clutter, fewer distractions, and a smoother path to the things that truly matter in your day.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(32.dp))
//            Text(
//                text = "Notifications let us:\n• Remind you of upcoming events\n• Keep Focus Mode active in the background\n• Deliver important updates instantly",
//                style = MaterialTheme.typography.bodyMedium,
//                textAlign = TextAlign.Start,
//                color = MaterialTheme.colorScheme.onBackground
//            )
//            Button(
//                modifier = Modifier.fillMaxWidth(.8f).heightIn(min = 48.dp).padding(horizontal=15.dp),
//                onClick = {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//                    } else{
//                        navigateToNextPage()
//                    }
//                },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.onBackground
//                )
//            ) {
//                Text(text = "Enable notifications", color = MaterialTheme.colorScheme.background)
//            }
            Spacer(modifier = Modifier.height(48.dp))

        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)){
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = navigateToPrev,
                    content = {
                        Text("PREVIOUS",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                )
                TextButton(onClick = navigateToNextPage,
                    content = {
                        Text("FINISH",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                )
            }
        }
    }
}