package com.example.android_launcher.presentation.screens.home.home.apps

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_launcher.domain.repository.AppsRepository
import com.example.android_launcher.utils.formatTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UsageInfo(
    val name: String,
    val packageName: String,
    val timeFormat: String?,
    val difference: Long?,
    val usagePerDay: List<Long>?=emptyList()
)
class BlockingAppViewModel(val context: Context,private val appsRepository: AppsRepository): ViewModel() {
    private val _appStats = MutableStateFlow<UsageInfo?>(null)
    val appStats: StateFlow<UsageInfo?> = _appStats.asStateFlow()

    private val _allAppsStats = MutableStateFlow<List<UsageInfo>>(emptyList())
    val allAppsStats = _allAppsStats.asStateFlow()
    val packageManager: PackageManager? = context.packageManager

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getAppUsageStats(packageName: String) {
        viewModelScope.launch {
//            val appsPkgNames = appsRepository.getAllApps().map { it.packageName }
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

            val endTime = System.currentTimeMillis()
            val startTime = endTime - 1000L * 60 * 60 * 24 * 7 // Last 7 days

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
            val statsF = stats.filter { it.packageName == packageName }

            _appStats.value = UsageInfo(
                name = getAppName(packageName),
                packageName = packageName,
                difference = statsF.sumOf { it.totalTimeVisible },
                timeFormat = formatTime(statsF.sumOf { it.totalTimeVisible })
            )

            // Filter for the specific app
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getAllAppsUsageStats(){
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000L * 60 * 60 * 24 * 7 // Last 7 days

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        val usageByApp = stats
            .groupBy { it.packageName }
            .mapValues { (_, usageList) ->
                usageList.sumOf { it.totalTimeVisible }
            }

        val topPackages = usageByApp.toList().sortedByDescending { (_, totalUsage) -> totalUsage }.take(21)

        val totalStats = topPackages.mapNotNull { (pkgName, totalUsage) ->
            if (isSystemApp(context, pkgName)) return@mapNotNull null

            val usageList = stats.filter { it.packageName == pkgName }
            UsageInfo(
                name = getAppName(pkgName),
                difference = totalUsage,
                usagePerDay = usageList.map { it.totalTimeVisible },
                timeFormat = formatTime(totalUsage),
                packageName = pkgName
            )
        }
        _allAppsStats.value = totalStats
    }
    fun blockUnblockAppFc(packageName: String,blocked: Int,blockReleaseDate: String?){
        viewModelScope.launch(Dispatchers.IO) {
//            if (app.isPinned == true) {
//                appsRepository.pinUnpinApp(packageName, pinned = 0)
//            }
            appsRepository.blockUnblockApp(packageName, blocked, blockReleaseDate)

        }
    }
    private  fun getAppName(packageName: String): String{
        val info = packageManager?.getApplicationInfo(packageName,0) as ApplicationInfo
        return packageManager.getApplicationLabel(info) as String
    }
    private fun isSystemApp(context: Context, packageName: String): Boolean {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        } catch (e: PackageManager.NameNotFoundException) {
            true // Treat unknown packages as system apps just to be safe
        }
    }
}