package com.example.android_launcher.presentation.screens.home.apps

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_launcher.domain.models.App
import com.example.android_launcher.domain.repository.AppsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BlockingAppViewModel(val context: Context,private val appsRepository: AppsRepository): ViewModel() {

    private val _appStats = MutableStateFlow<UsageStats?>(null)
    val appStats: StateFlow<UsageStats?> = _appStats.asStateFlow()
    fun getAppUsageStats(packageName: String) {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 60 * 24 // Last 24 hours

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        // Filter for the specific app
        val statsF = stats.find { it.packageName == packageName }
        _appStats.value = statsF
    }
    fun blockUnblockAppFc(app: App,blocked: Int){
        viewModelScope.launch(Dispatchers.IO) {
            if (app.isPinned == true) {
                appsRepository.pinUnpinApp(app.packageName, 0)
            }
            appsRepository.blockUnblockApp(app.packageName, blocked)

        }
    }
}