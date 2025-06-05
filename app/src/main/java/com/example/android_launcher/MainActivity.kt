package com.example.android_launcher

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.android_launcher.presentation.screens.home.HomeNavigator
import com.example.android_launcher.presentation.screens.onboarding.OnboardingPage
import com.example.android_launcher.ui.theme.AndroidlauncherTheme
import kotlinx.serialization.Serializable

@Serializable data object HomeNavigation
@Serializable data object Onboarding

class MainActivity : ComponentActivity() {
    private lateinit var myReceiver: MyBroadCastReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myReceiver = MyBroadCastReceiver()
        val filters = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(myReceiver, filters)
        enableEdgeToEdge()
        enableImmersiveMode()
        setContent {
            val backStack = remember { mutableStateListOf<Any>(Onboarding) }

            AndroidlauncherTheme {
                Surface(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.background), shape = RectangleShape) {
                    val navController = rememberNavController()
                    NavHost(navController = navController,startDestination = HomeNavigation){
                        composable<Onboarding> {
                            OnboardingPage()
                        }
                        composable<HomeNavigation> {
                            HomeNavigator()
                        }
                    }
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