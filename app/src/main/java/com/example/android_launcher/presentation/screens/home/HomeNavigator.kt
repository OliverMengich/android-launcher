package com.example.android_launcher.presentation.screens.home

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.android_launcher.R
import com.example.android_launcher.presentation.screens.home.apps.AppsPage
import com.example.android_launcher.presentation.screens.home.calendar.CalendarPage
import com.example.android_launcher.presentation.screens.home.home.HomePage
import kotlinx.serialization.Serializable

@Serializable sealed interface Screen
@Serializable data object HomeScreen: Screen
@Serializable data object AppsScreen: Screen
@Serializable data object CalendarScreen: Screen

@Composable
fun HomeNavigator(){
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    label = { Text("Apps") },
                    onClick = {
                        navController.navigate(route = AppsScreen)
                    },
                    icon = {
                        Icon(modifier = Modifier.size(20.dp), painter = painterResource(id=R.drawable.ic_dashboard), contentDescription = null)
                    },
                    selected = false,
                )
                NavigationBarItem(
                    label = { Text("Home") },
                    onClick = {
                        navController.navigate(route = HomeScreen)
                    },
                    icon = {
                        Icon(Icons.Default.Home, contentDescription = null,modifier = Modifier.size(20.dp), tint = if(isSystemInDarkTheme()) Color.White else Color.Black)
                    },
                    selected = true,
                )
                NavigationBarItem(
                    label = { Text("Calendar") },
                    onClick = {
                        navController.navigate(route = CalendarScreen)
                    },
                    icon = {
                        Icon(Icons.Default.DateRange, contentDescription = null,modifier = Modifier.size(20.dp), tint = if(isSystemInDarkTheme()) Color.White else Color.Black)
                    },
                    selected = false,
                )
            }
        }) { innerP->
        NavHost(navController=navController, modifier = Modifier.padding(innerP).padding(top=30.dp), startDestination= HomeScreen, ) {
            composable<HomeScreen> {
                HomePage()
            }
            composable<AppsScreen> {
                AppsPage()
            }
            composable<CalendarScreen> {
                CalendarPage()
            }
        }
    }
}