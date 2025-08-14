package com.example.android_launcher

import android.app.ComponentCaller
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.android_launcher.presentation.screens.home.HomeNavigator
import com.example.android_launcher.presentation.screens.onboarding.OnboardingPage
import com.example.android_launcher.services.AppMonitorService
import com.example.android_launcher.ui.theme.AndroidlauncherTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.androidx.viewmodel.ext.android.viewModel

@Serializable sealed interface Route
@Serializable data object HomeNavigation:Route
@Serializable data object Onboarding:Route

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
val OPEN_KEYBOARD = booleanPreferencesKey("open_keyboard_automatically")
val LOGGED_IN_USER_NAME = stringPreferencesKey(name = "user_name")

val PHONE_APP_PACKAGE = stringPreferencesKey(name="phone_app")
val CAMERA_APP_PACKAGE = stringPreferencesKey(name="camera_app")
val CLOCK_APP_PACKAGE = stringPreferencesKey(name="clock_app")

class MainActivity : ComponentActivity() {
    private lateinit var myReceiver: MyBroadCastReceiver
    private val mainViewModel: MainViewModel by viewModel()
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myReceiver = MyBroadCastReceiver()
        val filters = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
        }
        registerReceiver(myReceiver, filters)
//        installSplashScreen().setKeepOnScreenCondition {
//            mainViewModel.splashCondition
//        }
        enableEdgeToEdge()
        val intent = Intent(this, AppMonitorService::class.java)
        ContextCompat.startForegroundService(this, intent)
        val sharedRef = getSharedPreferences("settings_value", Context.MODE_PRIVATE)
        enableImmersiveMode()
        setContent {
            val isDarkMode = remember { mutableStateOf(false) }
            val x = isSystemInDarkTheme()
            LaunchedEffect(Unit) {
                val themeOpt = sharedRef.getString("THEME", "SYSTEM")
                isDarkMode.value = if(themeOpt.toString()=="SYSTEM"){
                    x
                }else if(themeOpt.toString()=="Dark"){
                    true
                }else{
                    false
                }
            }
            AndroidlauncherTheme(darkTheme = isDarkMode.value) {
                Scaffold(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.background)) { padding->
                    HomeNavigator(
                        padding=padding,
                        changeTheme = { opt->
                            isDarkMode.value = when (opt) {
                                "SYSTEM" -> {
                                    x
                                }
                                "Dark" -> {
                                    true
                                }
                                else -> {
                                    false
                                }
                            }
                            sharedRef.edit(commit = true) {
                                putString("THEME", opt)
                                apply()
                            }
//                            with(sharedRef.edit()) {
//                                putString("THEME", opt)
//                                apply()
//                            }
//                            scope.launch {
//
//                                with(sharedRef.edit()) {
//                                    putString("THEME", opt)
//                                    apply()
//                                }
//                            }
                        }
                    )
//                    NavHost(navController = navController,modifier = Modifier.fillMaxSize(), startDestination = mainViewModel.startDestination){
//                        composable<Onboarding> {
//                            OnboardingPage(
//                                padding=padding,
//                                finishNavigate = {
//                                    navController.navigate(route = HomeNavigation)
//                                }
//                            )
//                        }
//                        composable<HomeNavigation> {
//                            HomeNavigator(padding)
//                        }
//                    }
                }
            }
        }
    }

    private fun enableImmersiveMode(){
        val windowInsetsController = WindowCompat.getInsetsController(window,window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, windowInsets ->
            if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())) {
                windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
            } else {
                windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
            }
            ViewCompat.onApplyWindowInsets(view, windowInsets)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myReceiver)
    }
}

data class AppInfo(
    val name: String,
)
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidlauncherTheme {
        Greeting("Android")
    }
}