package com.example.android_launcher.presentation.screens.home.home

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.android_launcher.R
import com.example.android_launcher.domain.models.App
import com.example.android_launcher.presentation.screens.home.home.apps.AppsPage
import com.example.android_launcher.presentation.screens.home.home.calendar.CalendarPage
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Composable
fun LauncherHome(navigateToSettingsPage:()->Unit, navigateToBlockedApp:(App)->Unit,navigateToNewEvent:()->Unit, navigateToBlockingAppPage:(App)-> Unit) {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    val context = LocalContext.current
//    val sharedRef = context.getSharedPreferences("settings_value", Context.MODE_PRIVATE)
//    var isFocusModeActive by remember {
//        mutableStateOf(sharedRef.getBoolean("focus_mode", false))
//    }

//    DisposableEffect(Unit) {
//        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
//            when (key) {
//                "focus_mode" -> {
//                    isFocusModeActive = sharedPreferences.getBoolean(key, false)
//                }
//            }
//        }
//        sharedRef.registerOnSharedPreferenceChangeListener(listener)
//        onDispose {
//            sharedRef.unregisterOnSharedPreferenceChangeListener(listener)
//        }
//    }
//    LaunchedEffect(isFocusModeActive) {
//
//        if (isFocusModeActive){
//            val focusModeEndTime = sharedRef.getString("focus_mode_end_time","").let {
//                if (it.isNullOrEmpty()) null else LocalDateTime.parse(it)
//            }
//            if (focusModeEndTime!=null && LocalDateTime.now().isAfter(focusModeEndTime)){
//                sharedRef.edit{
//                    putBoolean("focus_mode", false)
//                    putString("focus_mode_end_time", null)
//                    apply()
//                }
//                isFocusModeActive = false
//            }
//        }else{
//            sharedRef.edit{
//                putBoolean("focus_mode", false)
//                remove("focus_mode_end_time")
//                apply()
//            }
//        }
//    }

    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    label = { Text(text="Apps") },
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(page=0)
                        }
                    },
                    icon = {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(id = R.drawable.ic_dashboard),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    selected = pagerState.currentPage==0,
                )
                NavigationBarItem(
                    label = { Text("Home") },
                    onClick = {
//                        if (isFocusModeActive){
//                            Toast.makeText(context, "Focus mode is active", Toast.LENGTH_SHORT).show()
//                            return@NavigationBarItem
//                        }
                        scope.launch {
                            pagerState.animateScrollToPage(page = 1)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    selected = pagerState.currentPage==1,
                )
                NavigationBarItem(
                    label = { Text(text="Calendar") },
                    onClick = {
//                        if (isFocusModeActive){
//                            Toast.makeText(context, "Focus mode is active", Toast.LENGTH_SHORT).show()
//                            return@NavigationBarItem
//                        }
                        scope.launch {
                            pagerState.animateScrollToPage(page = 2)
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    selected = pagerState.currentPage==2,
                )
            }
        }
    ) { innerP->
        HorizontalPager(state = pagerState,  modifier = Modifier.fillMaxSize().padding(paddingValues = innerP)) {index->
            when(index){
                0->{
                    AppsPage(
                        navigateToBlockedApp=navigateToBlockedApp,
                        navigateToBlockingAppPage=navigateToBlockingAppPage,
                        isFocused = pagerState.currentPage==0,
                        navigateToHome = {
                            scope.launch {
                                pagerState.animateScrollToPage(page = 1)
                            }
                        }
                    )
                }
                1->{
                    HomePage(navigateToSettingsPage = navigateToSettingsPage, )
                }
                2->{
                    CalendarPage(
                        navigateToNewEvent=navigateToNewEvent,
                        navigateToHome = {
                            scope.launch {
                                pagerState.animateScrollToPage(page = 1)
                            }
                        }
                    )
                }
            }
        }
    }
}