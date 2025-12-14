package com.example.android_launcher.presentation.screens.home.settings

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.android_launcher.CAMERA_APP_PACKAGE
import com.example.android_launcher.OPEN_KEYBOARD
import com.example.android_launcher.OnboardingActivity
import com.example.android_launcher.R
import com.example.android_launcher.dataStore
import com.example.android_launcher.domain.models.App
import com.example.android_launcher.domain.models.AppFonts
import com.example.android_launcher.domain.models.DisplaySettings
import com.example.android_launcher.presentation.components.AppItem
import com.example.android_launcher.presentation.components.DateTimePickerItem
import com.example.android_launcher.presentation.screens.home.SharedViewModel
import com.example.android_launcher.services.MyAccessibilityService
import com.example.android_launcher.ui.theme.checkedThumbColor
import com.example.android_launcher.ui.theme.checkedTrackColor
import com.example.android_launcher.ui.theme.uncheckedThumbColor
import com.example.android_launcher.ui.theme.uncheckedTrackColor
import com.example.android_launcher.utils.appFonts
import com.example.android_launcher.utils.dateFormatOptions
import com.example.android_launcher.utils.formatIsoTimeToFriendly
import com.example.android_launcher.utils.timeFormatOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsPage(modifier: Modifier = Modifier, sharedViewModel: SharedViewModel = koinViewModel(), viewModel: SettingsViewModel = koinViewModel(),  navigateToBlockingAppPage:(App)->Unit, navigateHome:()->Unit) {
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

    val user = Firebase.auth.currentUser

    var showChildren by remember { mutableStateOf(false) }

    LazyColumn(modifier = modifier.fillMaxSize().padding(bottom = 40.dp)) {
        item {
            Column(Modifier.fillMaxWidth().padding(top = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (user?.photoUrl != null) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "User profile photo",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.dp,
                                color = Color.Transparent,
                                shape = CircleShape
                            )
                    )
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

                if (user == null) {
                    Button(
                        onClick = {
                            val intent = Intent(context, OnboardingActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.putExtra("active_page", 1)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                        )
                    ) {
                        Text(text = "Sign Up / Login")
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
                if (user != null) {
                    AccordionItem(title = "Profile") {
                        Text(
                            text = user.displayName.toString(),
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
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
                                    modifier = Modifier.size(20.dp).rotate(180f),
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
                    .clickable { showChildren = !showChildren }
                    .padding(start = 20.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Hidden Apps", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Icon(
                    painter = painterResource(id = R.drawable.ic_down),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(if (showChildren) 0f else -90f),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            AnimatedVisibility(visible = showChildren, modifier = Modifier.padding(top = 5.dp,bottom=5.dp,start = 20.dp)) {
                if (hiddenApps.isEmpty()) {
                    Text("No hidden apps", Modifier.padding(start = 20.dp))
                } else {
                    Column(Modifier) {
                        hiddenApps.forEach { ap ->
                            AppItem(
                                onClick = {
                                    sharedViewModel.launchApp(app=ap)
                                },
                                ap = ap,
                                onHideApp = {
                                    scope.launch {
                                        sharedViewModel.hideUnhideAppFc(app=ap, hidden=0)
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
//        item {
//            AccordionItem(title = "Focus Mode") {
//                Column(Modifier.padding(horizontal = 10.dp),horizontalAlignment = Alignment.CenterHorizontally) {
//                    if(!isFocusMode){
//                        Text(
//                            text = "Focus Mode helps you take control of your time by minimizing distractions and limiting phone use while you work, study, or rest.\n"+
//                                    "When Focus Mode is on, your phone stays locked for everything except essential phone calls — no apps, no notifications, just uninterrupted focus. Once a session starts, you can’t switch it off until the timer ends, helping you stay fully committed to your goals.\n" +
//                                    "Set custom focus sessions, track your progress, and build mindful digital habits that last. Whether it’s a quick study session or a deep work block, Focus Mode keeps you centered on what truly matters."
//                        )
//                        DateTimePickerItem(
//                            callBack = {isDt->
//                                isoDateTime = isDt
//                            },
//                            datePickerHeadLine = {
//                                Text(
//                                    text = "Focus until ${formatIsoTimeToFriendly(input = isoDateTime)}.",
//                                    fontSize = 20.sp,
//                                    fontWeight = FontWeight.Medium,
//                                    textAlign = TextAlign.Center
//                                )
//                            }
//                        )
//                        Button(
//                            modifier=Modifier.fillMaxWidth(.5f).padding(horizontal=4.dp),
//                            onClick = {
//                                viewModel.enableFocusModeHandler(isoDateTime)
//                                navigateHome()
//                            },
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = MaterialTheme.colorScheme.onBackground,
//                            ),
//                        ) {
//                            Text(text = "Enable Focus Mode", color = MaterialTheme.colorScheme.background,)
//                        }
//                    }else{
//                        Text(text = "Focus Mode is already enabled")
//                    }
//                }
//            }
//        }
        item{
            AccordionItem(title="Precision Mode") {
                Column(Modifier.padding(horizontal=10.dp)){
                    Text(text="By enabling precision mode, you will be able to monitor apps even more and also block websites.")
                    SwitchItem(
                        modifier = Modifier.padding(vertical = 10.dp),
                        title = if(isAccessibilityEnabled) "Disable" else "Enable",
                        checked = isAccessibilityEnabled,
                        handleSwitchToggled = {
                            val precisionIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            Toast.makeText(context, "Please enable Accessibility permission", Toast.LENGTH_LONG).show()
                            context.startActivity(precisionIntent)
                            isAccessibilityEnabled = isAccessibilityServiceEnabled(context, service=MyAccessibilityService::class.java)

                        }
                    )
                }
            }
        }
        item{
            Column(
                Modifier.fillMaxWidth().padding(vertical = 10.dp).clickable { viewModel.handleNavigateToSettings() }
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 20.dp, end = 18.dp, top = 7.dp, bottom = 7.dp),
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
            AccordionItem(title="Exit Planara Launcher") {
                Column {
                    Text(text="Do you wish to exit Planara Launcher? You can uninstall Planara Launcher and use the default launcher or choose a different launcher.")
                    Row(Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.spacedBy(10.dp),verticalAlignment = Alignment.CenterVertically){
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
    }
}

@Composable
fun AccordionItem(title: String,children: @Composable () ->Unit){
    var showChildren by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().clickable { showChildren = !showChildren }.padding(start = 20.dp, end = 18.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
            Text(modifier=Modifier.padding(vertical = 8.dp),text=title,fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Icon(
                painter = painterResource(id = R.drawable.ic_down),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { showChildren = !showChildren }
                    .rotate(degrees = if (showChildren) { 0f } else { -90f }),
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
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 18.dp),
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