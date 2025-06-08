package com.example.android_launcher.presentation.screens.home

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_launcher.domain.models.App
import com.example.android_launcher.domain.repository.AppsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class SharedViewModel(private val appsRepository: AppsRepository, val context: Context): ViewModel() {
    val pm: PackageManager? = context.packageManager
    private val _apps = MutableStateFlow<List<App>>(emptyList())
    val apps: StateFlow<List<App>> = _apps.asStateFlow()

    private val _pinnedApps = MutableStateFlow<List<App>>(emptyList())
    val pinnedApps: StateFlow<List<App>> = _pinnedApps.asStateFlow()
    fun getPinnedApps(){
        val pinApps = appsRepository.getPinnedApps()
        _pinnedApps.value = pinApps
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("getting_app","apps")
            getApps()
            getPinnedApps()
        }
    }

    fun getApps(){
        Log.d("getting_app","before fetched")
        val ftApps = appsRepository.getAllApps()
        Log.d("getting_app","apps=${ftApps.size}")
        if (ftApps.isEmpty()){
            loadApps()
        }else{
            _apps.value = ftApps
        }
    }
    suspend fun blockAppFc(appId: Int){
        appsRepository.blockApp(appId)
    }
    fun pinApp(appId: Int){
        viewModelScope.launch(Dispatchers.IO) {
            appsRepository.pinApp(appId)
            getPinnedApps()
        }
    }
    suspend fun hideAppFc(appId: Int){
        appsRepository.hideApp(appId)
        getApps()
    }
    fun addNewInstalledApp(app: App){
        viewModelScope.launch {
            appsRepository.newAppInstalled(app)
            getApps()
        }
    }
    fun removeUninstalledApp(packageName: String){
        viewModelScope.launch {
            appsRepository.removeUninstalled(packageName)
            getApps()
        }
    }
    fun loadApps(){
        val packages = mutableListOf<ApplicationInfo>()
        val allPacs = pm?.getInstalledApplications(PackageManager.GET_META_DATA)
        if (allPacs != null) {
            for (pac in allPacs) {
                if (pm.getLaunchIntentForPackage(pac.packageName) != null) {
                    val appInfo = pm.getApplicationInfo(pac.packageName, 0)
                    packages.add(pm.getApplicationInfo(pac.packageName, 0))
                }
            }
        }
        packages.sortBy { pm?.getApplicationLabel(it).toString() }
        val newApps = packages.map { ap ->
            App(
                id = Random.nextInt(100000, 1000000),
                packageName = ap.packageName,
                name = pm?.getApplicationLabel(ap).toString(),
            )
        }
        appsRepository.insertApps(newApps)
        _apps.value = newApps

    }

    fun getAppUsageStats(context: Context, packageName: String): UsageStats? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 60 * 24 // Last 24 hours

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        // Filter for the specific app
        return stats.find { it.packageName == packageName }
    }

    fun launchApp(packageName: String){
        Log.d("pkgNme","package name=$packageName")
        val intent = pm?.getLaunchIntentForPackage(packageName)
        if (intent != null){
            context.startActivity(intent)
        }else{
            Toast.makeText(context, "Can't Launch this app", Toast.LENGTH_SHORT).show()
        }
    }
}