package com.example.android_launcher


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.android_launcher.services.AppMonitorService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LauncherActivity: ComponentActivity() {
//    private val mainViewModel: MainViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        installSplashScreen().setKeepOnScreenCondition {
//            mainViewModel.splashCondition
//        }
        enableEdgeToEdge()
        val intent = Intent(this, AppMonitorService::class.java)
        ContextCompat.startForegroundService(this, intent)
        enableImmersiveMode()

        lifecycleScope.launch {
            val localManager = dataStore.data.first()
            val startIntent = if (localManager.isLoggedIn) {
                Intent(this@LauncherActivity, MainActivity::class.java)
            } else {
                Intent(this@LauncherActivity, OnboardingActivity::class.java)
            }
            startActivity(startIntent)
            finish()
        }
//        val sharedRef = getSharedPreferences("settings_value", Context.MODE_PRIVATE)
//        val isLoggedInUser = sharedRef.getBoolean("IS_AUTHENTICATED",false)
//        Log.d("start_intent"," is user logged in=$isLoggedInUser")
//        val startIntent = if (isLoggedInUser){
//            Intent(this, MainActivity::class.java).apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
//        }else{
//            Intent(this, OnboardingActivity::class.java).apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
//        }
//        Log.d("start_intent","start intent is $startIntent")
//        startActivity(startIntent)
//        finish()
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