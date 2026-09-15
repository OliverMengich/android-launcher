package com.planara.android_launcher.presentation.screens.home.settings

import TimePickerDialog
import android.accessibilityservice.AccessibilityService
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.planara.android_launcher.OnboardingActivity
import com.planara.android_launcher.R
import com.planara.android_launcher.dataStore
import com.planara.android_launcher.domain.models.App
import com.planara.android_launcher.domain.models.AppFonts
import com.planara.android_launcher.presentation.components.AppItem
import com.planara.android_launcher.presentation.screens.home.SharedViewModel
import com.planara.android_launcher.services.MyAccessibilityService
import com.planara.android_launcher.ui.theme.checkedThumbColor
import com.planara.android_launcher.ui.theme.checkedTrackColor
import com.planara.android_launcher.ui.theme.uncheckedThumbColor
import com.planara.android_launcher.ui.theme.uncheckedTrackColor
import com.planara.android_launcher.utils.appFonts
import com.planara.android_launcher.utils.dateFormatOptions
import com.planara.android_launcher.utils.timeFormatOptions
import com.planara.android_launcher.LocalUserSettings
import com.planara.android_launcher.domain.models.BlockType
import com.planara.android_launcher.domain.models.UsageTime
import com.planara.android_launcher.presentation.components.SharedDialog
import com.planara.android_launcher.presentation.components.rememberDialogState
import com.planara.android_launcher.presentation.screens.home.home.apps.BlockState

import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

sealed interface DialogStep {
    data object StepOne : DialogStep
    data class StepTwo(val details: String) : DialogStep
    data object Success : DialogStep
}

@Composable
fun SettingsPage(modifier: Modifier = Modifier, sharedViewModel: SharedViewModel = koinViewModel(), viewModel: SettingsViewModel = koinViewModel(),  navigateToBlockingAppPage:(App)->Unit, navigateHome:()->Unit, navigateToFocusModePage:()->Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localManagerData by sharedViewModel.localManagerData.collectAsStateWithLifecycle()

    val lifeCycleOwner = LocalLifecycleOwner.current

    var isAccessibilityEnabled by remember {
        mutableStateOf(
            value=isAccessibilityServiceEnabled(context, service= MyAccessibilityService::class.java)
        )
    }
    DisposableEffect(key1 = lifeCycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if(event == Lifecycle.Event.ON_RESUME) {
                isAccessibilityEnabled = isAccessibilityServiceEnabled(context, service = MyAccessibilityService::class.java)
            }
        }
        lifeCycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val hiddenApps = sharedViewModel.hiddenApps.collectAsState().value
    LaunchedEffect(Unit) {
        sharedViewModel.getHiddenApps()
    }

    var showChildren by remember { mutableStateOf(false) }

    fun showAccessibilityDisclosureDialog(onAccept: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Accessibility Permission Required")
            .setMessage(
                "Planara Launcher uses the Accessibility Service API to detect active URLs in supported web browsers. " +
                        "\n\nWhy this is needed:\n" +
                        "• To identify when a blocked domain or distracting website is opened during Focus Mode.\n" +
                        "• To redirect you away from restricted sites to help you stay focused.\n\n" +
                        "Privacy Notice:\n" +
                        "• URL data is processed locally on your device in real-time.\n" +
                        "• We do not collect, store, or transmit your browsing history, personal data, or passwords."
            )
            .setPositiveButton("Agree & Enable") { _, _ ->
                onAccept()
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
            .setNegativeButton("Not Now", null)
            .setCancelable(false)
            .show()
    }
    val dialogState = rememberDialogState()
    SharedDialog(state = dialogState,modifier = Modifier.fillMaxWidth())
    val settings = LocalUserSettings.current
    // 2. Registers the photo picker activity launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        scope.launch {
            uri.let {
                if (it != null) {
                    Toast.makeText(context, "Selected photo: $it", Toast.LENGTH_SHORT).show()
                    context.dataStore.updateData { lc->
                        lc.copy(
                            displaySettings = lc.displaySettings.copy(
                                profileImagePath = uri?.toString() ?: ""
                            )
                        )
                    }
                } else {
                    Toast.makeText(context, "No photo selected", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LazyColumn(modifier.fillMaxSize().padding(bottom = 40.dp)) {
        item {
            Column(Modifier
                .fillMaxWidth()
                .padding(top = 0.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (settings.profileImagePath.isNotEmpty()) {
                    Box(
                        modifier = Modifier.size(150.dp) // Outer container bounds match the image
                    ) {
                        AsyncImage(
                            model = settings.profileImagePath,
                            contentDescription = "User profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                        )

                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(20.dp) // Generous touch target
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change profile photo",
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .border(
                                shape = CircleShape,
                                border = BorderStroke(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            .background(Color.Gray)
                    )
                }
                if (settings.profileImagePath.isEmpty()) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
//                            val intent = Intent(context, OnboardingActivity::class.java)
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                            intent.putExtra("active_page", 1)
//                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                        )
                    ) {
                        Text(text = "Select Display Photo")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Display",
                modifier = Modifier.padding(start = 20.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp
            )
            Column(Modifier.fillMaxWidth()) {
                AccordionItem(title = "Theme") {
                    RadioButtonSingleSelection(
                        setItem = localManagerData.displaySettings.theme,
                        onOptionClicked = { opt ->
                            scope.launch {
                                context.dataStore.updateData {
                                    it.copy(
                                        displaySettings = it.displaySettings.copy(
                                            theme = opt,
                                        )
                                    )
                                }
                            }
                        },
                        items = listOf(
                            ItemProps("SYSTEM") {
                                Text("Same as Device", Modifier.padding(start = 10.dp))
                            },
                            ItemProps("Dark") {
                                Icon(
                                    painter = painterResource(id = R.drawable.moon),
                                    contentDescription = "Dark icon",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .rotate(180f),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                                Text("Dark Mode")
                            },
                            ItemProps("Light") {
                                Icon(
                                    painter = painterResource(id = R.drawable.sun),
                                    contentDescription = "Light icon",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                                Text("Light Mode")
                            }
                        )
                    )
                }

                AccordionItem("Time Format") {
                    RadioButtonSingleSelection(
                        setItem = localManagerData.displaySettings.timeFormat ,
                        onOptionClicked = { opt ->
                            scope.launch {

                                context.dataStore.updateData {
                                    it.copy(
                                        displaySettings = it.displaySettings.copy(
                                            timeFormat = opt,
                                        )
                                    )
                                }
                            }
                        },
                        items =timeFormatOptions.entries.map { (key,value)->
                            ItemProps(
                                title= key,
                                children = {
                                    Text(text=value,Modifier.padding(10.dp))
                                }
                            )
                        }
                    )
                }

                AccordionItem("Date Format") {
                    RadioButtonSingleSelection(
                        setItem = localManagerData.displaySettings.dateFormat,
                        onOptionClicked = { opt ->
                            scope.launch {
                                context.dataStore.updateData {
                                    it.copy(
                                        displaySettings = it.displaySettings.copy(
                                            dateFormat = opt,
                                        )
                                    )
                                }
                            }
                        },
                        items = dateFormatOptions.entries.map { (key,value)->
                            ItemProps(
                                title= key,
                                children = {
                                    Text(text=value,Modifier.padding(10.dp))
                                }
                            )
                        }
                    )
                }
                AccordionItem(title="Choose font") {
                    RadioButtonSingleSelection(
                        setItem = localManagerData.displaySettings.currentFont.name,
                        onOptionClicked = { opt ->
                            scope.launch {
                                context.dataStore.updateData { prefs->
                                    prefs.copy(
                                        displaySettings = prefs.displaySettings.copy(
                                            currentFont = AppFonts.valueOf(value=opt),
                                        )
                                    )
                                }
                            }
                        },
                        items = appFonts.entries.map { (key,value)->
                            ItemProps(
                                title= key.toString(),
                                children = {
                                    Text(text=value,Modifier.padding(10.dp))
                                }
                            )
                        }
                    )
                }
                SwitchItem(
                    title = "Automatically Open Keyboard",
                    checked = localManagerData.displaySettings.autoOpenKeyboard,
                    handleSwitchToggled = { checked ->
                        scope.launch {
                            context.dataStore.updateData { prefs->
                                prefs.copy(
                                    displaySettings = prefs.displaySettings.copy(
                                        autoOpenKeyboard = checked,
                                    )
                                )
                            }
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
        }

        item {
            Text(
                text = "Settings",
                modifier = Modifier.padding(horizontal = 20.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        dialogState.show { controller->
                            Column(Modifier) {
                                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Hidden Apps", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier=Modifier.padding(10.dp))
                                    Icon(imageVector = Icons.Default.Close,modifier = Modifier.clickable{controller.dismiss()}.rotate(degrees = -90f),contentDescription = null)
                                }
                                HorizontalDivider()
                                if (hiddenApps.isEmpty()) {
                                    Box(Modifier.height(200.dp).fillMaxWidth().padding(10.dp), contentAlignment = Alignment.Center) {
                                        Text("No hidden apps", Modifier.padding(start = 20.dp))
                                    }
                                } else {
                                    Column(Modifier.padding(10.dp)) {
                                        hiddenApps.forEach { ap ->
                                            AppItem(
                                                onClick = {
                                                    sharedViewModel.launchApp(app=ap)
                                                },
                                                ap = ap,
                                                onHideApp = {
                                                    scope.launch {
                                                        sharedViewModel.hideUnhideAppFc(app=ap, false)
                                                    }
                                                },
                                                onUninstallApp = {
                                                    val intent = Intent(Intent.ACTION_DELETE)
                                                    intent.data = "package:${ap.packageName}".toUri()
                                                    context.startActivity(intent)
                                                },
                                                onBlockApp = {
                                                    navigateToBlockingAppPage(ap)
                                                },
                                                onPinApp = {
                                                    Toast.makeText(context, "You cannot pin a hidden app, unhide first", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .padding(start = 20.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Hidden Apps", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

        }
        item {
            // Focus session details live on the Focus Mode page only.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navigateToFocusModePage() }
                    .padding(start = 20.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Focus Mode", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        item{
            AccordionItem(title="Precision Mode") {
                Column(Modifier.padding(horizontal=10.dp)){
                    Text(text="By enabling precision mode, you will be able to monitor apps even more and also block websites.")
                    SwitchItem(
                        modifier = Modifier.padding(vertical = 10.dp),
                        title = if(isAccessibilityEnabled) "Disable" else "Enable",
                        checked = isAccessibilityEnabled,
                        handleSwitchToggled = {
//                            .setTitle("Accessibility Permission Required")
//                            .setMessage(
//                                "Planar Launcher uses the Accessibility Service API to detect active URLs in supported web browsers. " +
//                                        "\n\nWhy this is needed:\n" +
//                                        "• To identify when a blocked domain or distracting website is opened during Focus Mode.\n" +
//                                        "• To redirect you away from restricted sites to help you stay focused.\n\n" +
//                                        "Privacy Notice:\n" +
//                                        "• URL data is processed locally on your device in real-time.\n" +
//                                        "• We do not collect, store, or transmit your browsing history, personal data, or passwords."
//
                            dialogState.show { controller->
                                Column() {
                                    Text(
                                        text="Accessibility permission Required",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(10.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    HorizontalDivider()
                                    Text(
                                        text = "Planara Launcher uses the Accessibility Service API to detect active URLs in supported web browsers. " +
                                        "\n\nWhy this is needed:\n" +
                                        "• To identify when a blocked domain or distracting website is opened during Focus Mode.\n" +
                                        "• To redirect you away from restricted sites to help you stay focused.\n\n" +
                                        "Privacy Notice:\n" +
                                        "• URL data is processed locally on your device in real-time.\n" +
                                        "• We do not collect, store, or transmit your browsing history, personal data, or passwords.",
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                controller.dismiss()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.onBackground,
                                            )
                                        ) {
                                            Text("Cancel", color = MaterialTheme.colorScheme.background)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Button(
                                            onClick = {
                                                controller.dismiss()
                                                isAccessibilityEnabled = isAccessibilityServiceEnabled(context, service=MyAccessibilityService::class.java)
                                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.onBackground,
                                            )
                                        ) {
                                            Text("Enable", color = MaterialTheme.colorScheme.background)
                                        }
                                    }
                                }
                            }
//                            showAccessibilityDisclosureDialog {
//                                Toast.makeText(context, "Please enable Accessibility permission", Toast.LENGTH_LONG).show()
//                                isAccessibilityEnabled = isAccessibilityServiceEnabled(context, service=MyAccessibilityService::class.java)
//                            }
                        }
                    )
                }
            }
        }
        item{
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .clickable { viewModel.handleNavigateToSettings() }
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 18.dp, top = 7.dp, bottom = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Device Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                HorizontalDivider()
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        dialogState.show { controller->
                            Column {
                                Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Exit Planara Launcher", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier=Modifier.padding(10.dp))
                                    Icon(imageVector = Icons.Default.Close,modifier = Modifier.clickable{controller.dismiss()},contentDescription = null)
                                }
                                HorizontalDivider()
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 10.dp),
                                    text= "Do you wish to exit Planara Launcher? You can uninstall Planara Launcher and use the default launcher or choose a different launcher."
                                )
                                HorizontalDivider()
                                Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 10.dp),horizontalArrangement = Arrangement.spacedBy(10.dp),verticalAlignment = Alignment.CenterVertically){
                                    Button(
                                        onClick = {
                                            val packageName = context.packageName
                                            val intent = Intent(Intent.ACTION_DELETE)
                                            intent.data = "package:${packageName}".toUri()
                                            context.startActivity(intent)
                                        },
                                        Modifier.weight(.6f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    ){
                                        Text(text = "Uninstall")
                                    }
                                    Button(
                                        onClick = { viewModel.handleNavigateToSettings() },
                                        Modifier.weight(.6f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline)
                                    ){
                                        Text(text = "Exit")
                                    }
                                }
                            }
                        }
                    }
                    .padding(start = 20.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Exit Planara Launcher", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun AccordionItem(title: String,children: @Composable () ->Unit){
    var showChildren by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable { showChildren = !showChildren }
            .padding(start = 20.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(text=title,modifier=Modifier.padding(vertical = 8.dp),fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Icon(
                painter = painterResource(id = R.drawable.ic_down),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { showChildren = !showChildren }
                    .rotate(
                        degrees = if (showChildren) {
                            0f
                        } else {
                            -90f
                        }
                    ),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        AnimatedVisibility(visible = showChildren, modifier = Modifier.padding(horizontal = 20.dp)) {
            children()
        }
    }
}

@Composable
fun SwitchItem(modifier: Modifier=Modifier,title:String,checked: Boolean,handleSwitchToggled:(Boolean)->Unit={}){
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(text=title,fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Switch(
                checked = checked,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = checkedThumbColor,
                    uncheckedTrackColor = uncheckedTrackColor,
                    checkedTrackColor = checkedTrackColor,
                    uncheckedThumbColor = uncheckedThumbColor,
                    uncheckedBorderColor = uncheckedThumbColor,
                    checkedBorderColor = checkedThumbColor
                ),
                onCheckedChange = handleSwitchToggled,
                thumbContent = {
                    Icon(
                        imageVector = if(checked)Icons.Filled.Check else Icons.Filled.Close,
                        tint = if(checked) Color(color=0xFF03fa6e) else Color.White,
                        contentDescription = ""
                    )
                }
            )
        }
    }
}

data class ExpandableItem(
    val title: String,
    var isExpanded: Boolean = false
)

data class ItemProps(
    val title: String,
    val children: @Composable () -> Unit
)

@Composable
fun RadioButtonSingleSelection(modifier: Modifier = Modifier,items: List<ItemProps>,setItem: String,onOptionClicked:(String)->Unit) {
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(setItem) }

    Column(modifier=modifier.selectableGroup()) {
        items.forEach {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (it.title == selectedOption),
                        onClick = { onOptionSelected(it.title); onOptionClicked(it.title) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    colors = RadioButtonDefaults.colors(selectedColor =MaterialTheme.colorScheme.onBackground),
                    selected = (it.title == selectedOption),
                    modifier=Modifier.padding(end = 10.dp),
                    onClick = null
                )
                it.children()
            }
        }
    }
}
fun isAccessibilityServiceEnabled(
    context: Context,
    service: Class<out AccessibilityService>
): Boolean {
    val expectedComponentName = ComponentName(context, service)

    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )

    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServices ?: "")

    while (colonSplitter.hasNext()) {
        val componentNameString = colonSplitter.next()
        val enabledComponentName = ComponentName.unflattenFromString(componentNameString)
        if (enabledComponentName != null && enabledComponentName == expectedComponentName) {
            return true
        }
    }

    return false
}