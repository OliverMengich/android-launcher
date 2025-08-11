package com.example.android_launcher

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.android_launcher.presentation.screens.onboarding.OnboardingPage
import com.example.android_launcher.services.AppMonitorService
import com.example.android_launcher.ui.theme.AndroidlauncherTheme

class OnboardingActivity: ComponentActivity() {

    companion object {
        var isVisible = false

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isVisible = true
        Log.d("tag_here","intent created")
        enableEdgeToEdge()
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        enableImmersiveMode()
        val activePage= intent.getIntExtra("active_page",0)
        Log.d("active_page","active page=$activePage. Is visible=$isVisible")
        setContent {
            AndroidlauncherTheme {
                Scaffold(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.background)) { padding->
                    OnboardingPage(
                        finishNavigate = {},
                        padding = padding,
                        activePage=activePage
                    )
                }
            }
        }
    }

    override fun onPause() {
        Log.d("tag_here", "intent paused")
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        Log.d("tag_here", "intent resumed")
        isVisible = true
    }
    override fun onStart() {
        Log.d("tag_here","intent started")
        super.onStart()
        isVisible = true
    }

    override fun onStop() {
        isVisible = false
        Log.d("tag_here","intent stopped")
        super.onStop()
    }

    override fun onDestroy() {
        isVisible = false
        Log.d("tag_here","intent destroyed")
        super.onDestroy()
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