package com.example.android_launcher.presentation.screens.home.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import coil3.compose.AsyncImage
import com.example.android_launcher.CAMERA_APP_PACKAGE
import com.example.android_launcher.OPEN_KEYBOARD
import com.example.android_launcher.OnboardingActivity
import com.example.android_launcher.R
import com.example.android_launcher.dataStore
import com.example.android_launcher.presentation.components.AppItem
import com.example.android_launcher.presentation.screens.home.SharedViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsPage(modifier: Modifier=Modifier, sharedViewModel: SharedViewModel = koinViewModel(), viewModel: SettingsViewModel= koinViewModel(), changeTheme:(String)->Unit){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedRef = context.getSharedPreferences("settings_value", Context.MODE_PRIVATE)
    val isOpen = remember { mutableStateOf(false) }
    val themeOption = remember { mutableStateOf("") }
    val timeFormat = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val isOpenKeyboard = sharedRef.getBoolean("IS_OPEN_KEYBOARD", false)
        val themeOpt = sharedRef.getString("THEME", "SYSTEM")
        val tmFmt = sharedRef.getString("TIME_FORMAT", "24hr")
        themeOption.value = themeOpt.toString()
        isOpen.value = isOpenKeyboard
        timeFormat.value = tmFmt.toString()
    }
    val verticalScroll = rememberScrollState()

    val hiddenApps = viewModel.hiddenApps.collectAsState().value
    val user = Firebase.auth.currentUser
    Column(modifier=modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(top=30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if(user?.photoUrl!=null){
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "User profile photo",
                    modifier = Modifier.size(size=200.dp).clip(CircleShape).border(width = 1.dp, color = Color.Transparent, shape = CircleShape)
                )
            }else {
                Box(
                    modifier = Modifier
                        .size(size=200.dp)
                        .clip(CircleShape)
                        .border(
                            shape = CircleShape,
                            border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.onBackground)
                        )
                        .background(Color.Gray)
                        .padding(top = 30.dp),
                ) {}
            }
            if (user ==null) {
                Button({
                    val intent = Intent(context, OnboardingActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("active_page", 1)
                    context.startActivity(intent)
                }) {
                    Text("Sign Up/Login")
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Column() {
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text(text="Display",Modifier.padding(start = 10.dp), fontWeight = FontWeight.Bold, fontSize = 25.sp)
            Column(Modifier.fillMaxWidth()) {
                if (user !=null) {
                    AccordionItem(title = "Edit Profile") {
                        Text(text=user.displayName.toString(),Modifier.padding(horizontal = 20.dp))
                    }
                }
                AccordionItem(title="Theme") {
                    RadioButtonSingleSelection(
                        setItem = themeOption.value,
                        onOptionClicked = { opt->
                            changeTheme(opt)
                            scope.launch {
                                val sharedPref = context.getSharedPreferences("settings_value", Context.MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    putString("THEME", opt)
                                    apply()
                                }
                            }
                        },
                        items = listOf(
                            ItemProps(
                                title = "SYSTEM",
                                children = {
                                    Text(text="Same as Device",modifier=Modifier.padding(start = 10.dp))
                                }
                            ),
                            ItemProps(
                                title = "Dark",
                                children = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.moon),
                                        contentDescription = "Dark icon",
                                        modifier = Modifier.size(20.dp).rotate(180f),
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(text="Dark Mode",)
                                }
                            ),
                            ItemProps(
                                title = "Light",
                                children = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.sun),
                                        contentDescription = "Light icon",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(text="Light Mode")
                                }
                            )
                        )
                    )
                }
                AccordionItem("Time Format") {
                    RadioButtonSingleSelection(
                        setItem = timeFormat.value,
                        onOptionClicked = { opt->
                            scope.launch {
                                val sharedPref = context.getSharedPreferences("settings_value", Context.MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    putString("TIME_FORMAT", opt)
                                    apply()
                                }
                            }
                        },
                        items = listOf(
                            ItemProps(
                                title = "12hr",
                                children = {
                                    TextButton(
                                        onClick = {},
                                        content = {
                                            Text(
                                                text="12 hr format",
                                                modifier = Modifier.padding(10.dp),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onBackground,
                                            )
                                        }
                                    )
                                }
                            ),
                            ItemProps(
                                title = "24hr",
                                children = {
                                    TextButton(
                                        onClick = {},
                                        content = {
                                            Text(
                                                text="24 hr format",
                                                modifier = Modifier.padding(10.dp),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    )
                                }
                            ),
                        )
                    )
                }
                SwitchItem(
                    title="Automatically Open Keyboard",
                    checked=  isOpen.value,
                    handleSwitchToggled = { checked->
                        scope.launch {
                            val sharedPref = context.getSharedPreferences("settings_value", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putBoolean("IS_OPEN_KEYBOARD", checked)
                                apply()
                            }
                            isOpen.value = checked
                        }
                    }
                )
            }
        }
        var showChildren by remember { mutableStateOf(false) }

        Column() {
            HorizontalDivider()
            Text(text="Settings",Modifier.padding(horizontal = 20.dp), fontWeight = FontWeight.Bold, fontSize = 25.sp)

            LazyColumn {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showChildren = !showChildren }
                            .padding(start = 20.dp, end = 18.dp, top = 9.dp, bottom = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 8.dp),
                            text = "Hidden Apps",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_down),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { showChildren = !showChildren }
                                .rotate(if (showChildren) 0f else -90f),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                item {
                    AnimatedVisibility(visible = showChildren) {
                        Column(Modifier.padding(horizontal = 10.dp)) {
                            hiddenApps.forEach { ap ->
                                AppItem(
                                    onClick = { sharedViewModel.launchApp(ap) },
                                    ap = ap,
                                    onHideApp = {
                                        scope.launch {
                                            sharedViewModel.hideUnhideAppFc(ap, 0)
                                        }
                                    },
                                    onUninstallApp = {
                                        val intent = Intent(Intent.ACTION_DELETE)
                                        intent.data = "package:${ap.packageName}".toUri()
                                        context.startActivity(intent)
                                    },
                                    onBlockApp = {
                                        scope.launch {
                                            sharedViewModel.blockUnblockAppFc(ap,if(ap.isBlocked==true)0 else 1)
                                        }
                                    },
                                    onPinApp = {
                                        Toast.makeText(context, "You cannot pin a hidden app, unhide first", Toast.LENGTH_SHORT).show()
                                    },
                                )
                            }
                        }
                    }
                }
            }
            AccordionItem("Precision Mode") {
                Column(Modifier.padding(horizontal = 20.dp),) {
                    TextButton(
                        onClick={
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        content={
                            Text("Enable precision mode")
                        }
                    )
                }
            }
            Column(Modifier.fillMaxWidth()
                .clickable {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }) {
                Row(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 18.dp, top = 7.dp, bottom = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
                    Text(modifier= Modifier.padding(vertical = 8.dp),text="Device Settings",fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}
@Composable
fun AccordionItem(title: String,children: @Composable () ->Unit){
    var showChildren by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().clickable { showChildren = !showChildren }.padding(start = 20.dp, end = 18.dp, top = 9.dp, bottom = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
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
fun SwitchItem(title:String,checked: Boolean,handleSwitchToggled:(Boolean)->Unit){
    Column(Modifier.fillMaxWidth()) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
            Text(text=title,fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Switch(
                checked = checked,
                colors = SwitchDefaults.colors(
                    checkedIconColor = MaterialTheme.colorScheme.onBackground
                ),
                onCheckedChange = handleSwitchToggled
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
        items.forEach { it->
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