package com.planara.android_launcher


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.planara.android_launcher.domain.manager.LocalManager
import com.planara.android_launcher.ui.theme.AndroidlauncherTheme
import com.planara.android_launcher.utils.formatIsoTimeToFriendly
import com.planara.android_launcher.utils.formatTimeToRequiredFormat
import java.time.LocalTime

class BlockedAppActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appName = intent.getStringExtra("app_name")
        val message = intent.getStringExtra("message")
        val isFocusModeOn = intent.getBooleanExtra("focus_mode",false)
        val focusModeEndTime = intent.getStringExtra("focus_mode_end_time")?.let {LocalTime.parse(it)} ?: LocalTime.now()
        enableEdgeToEdge()
        enableImmersiveMode()
//        val sharedRef = getSharedPreferences("settings_value", Context.MODE_PRIVATE)
        setContent {

            val localManagerData = dataStore.data.collectAsStateWithLifecycle(initialValue = LocalManager()).value
            val systemDark = isSystemInDarkTheme()
            val isDarkMode = when (localManagerData.displaySettings.theme) {
                "SYSTEM" -> systemDark
                "Dark" -> true
                else -> false
            }
            AndroidlauncherTheme(darkTheme = isDarkMode, defaultFont = localManagerData.displaySettings.currentFont) {
                Scaffold(modifier = Modifier.fillMaxSize()) { p->
                    if(isFocusModeOn){
                        Column(modifier = Modifier.fillMaxSize().padding(p), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly){
                            Text("Focus Mode Active!", fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
                            Text("Focus mode is on keep focusing on your work, focus mode end at ${focusModeEndTime.formatTimeToRequiredFormat("hh:mm a")}")
                            Button(onClick={ finish() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onBackground,
                                ),
                                modifier = Modifier.fillMaxWidth(fraction=0.9f),
                            ){
                                Text(
                                    text="CLOSE",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(all=10.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.background,
                                )
                            }
                        }
                    } else{
                        Column(modifier = Modifier.fillMaxSize().padding(p), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
                            Text("Blocked $appName", fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
//                            Text(
//                                text="You block $appName until ${formatIsoTimeToFriendly(input=releaseDate)}. Digital detox is working, keep moving",
//                                modifier = Modifier.fillMaxWidth(fraction=.8f)
//                            )
                            Text(
                                text=message.toString(),
                                modifier = Modifier.fillMaxWidth(fraction=.8f)
                            )
                            Button(onClick={ finish() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onBackground,
                                ),
                                modifier = Modifier.fillMaxWidth(fraction=0.9f),
                            ){
                                Text(
                                    text="CLOSE",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(all=10.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.background,
                                )
                            }
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
}