package com.example.android_launcher.presentation.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.android_launcher.presentation.screens.home.home.apps.AppsPage
import com.example.android_launcher.presentation.screens.home.home.apps.BlockingAppPage
import com.example.android_launcher.presentation.screens.home.home.HomePage
import com.example.android_launcher.presentation.screens.home.home.LauncherHome
import com.example.android_launcher.presentation.screens.home.home.calendar.NewEventPage
import com.example.android_launcher.presentation.screens.home.settings.SettingsPage
import kotlinx.serialization.Serializable
import kotlin.toString

@Serializable sealed interface Screen
@Serializable data object LauncherHomeScreen: Screen
@Serializable data class BlockedAppScreen(val name: String,val blockReleaseDate: String?= "", val packageName: String): Screen
@Serializable data class BlockingAppScreen(val packageName: String): Screen
@Serializable data object NewEventScreen: Screen
@Serializable data object SettingsScreen: Screen

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun HomeNavigator(padding: PaddingValues, ){
    val navController = rememberNavController()
    NavHost(navController=navController, modifier = Modifier.fillMaxSize().padding(top=30.dp), startDestination= LauncherHomeScreen, ) {
        composable<LauncherHomeScreen> {
            LauncherHome(
                navigateToSettingsPage = {
                    navController.navigate(route=SettingsScreen)
                },
                navigateToBlockedApp = {ap->
                    navController.navigate(route = BlockedAppScreen(name = ap.name, packageName = ap.packageName, blockReleaseDate = ap.blockReleaseDate))
                },
                navigateToBlockingAppPage = {ap->
                    navController.navigate(route = BlockingAppScreen(packageName = ap.packageName))
                },
                navigateToNewEvent={
                    navController.navigate(route= NewEventScreen)
                }
            )
        }
        composable<SettingsScreen> {
            SettingsPage(
                modifier = Modifier.padding(paddingValues=padding),
                navigateHome={
                    navController.navigate(route = LauncherHomeScreen)
                },
                navigateToBlockingAppPage = {ap->
                    navController.navigate(route = BlockingAppScreen(packageName = ap.packageName))
                },
            )
        }
        composable<NewEventScreen> {
            NewEventPage(goBack = {navController.popBackStack()})
        }
        composable<BlockingAppScreen> { backState->
            val info = backState.toRoute<BlockingAppScreen>()
            BlockingAppPage(
                packageName = info.packageName,
                modifier = Modifier.padding(padding),
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }

}