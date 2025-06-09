package com.example.android_launcher.presentation.screens.home.apps

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BlockedAppViewModel(val context: Context): ViewModel() {

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
}