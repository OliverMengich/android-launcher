package com.example.android_launcher.presentation.screens.home

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_launcher.BlockedAppActivity
import com.example.android_launcher.CAMERA_APP_PACKAGE
import com.example.android_launcher.CLOCK_APP_PACKAGE
import com.example.android_launcher.IS_LOGGED_IN_KEY
import com.example.android_launcher.PHONE_APP_PACKAGE
import com.example.android_launcher.dataStore
import com.example.android_launcher.domain.models.App
import com.example.android_launcher.domain.repository.AppsRepository
import com.example.android_launcher.utils.isDatePassed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SharedViewModel(private val appsRepository: AppsRepository, val context: Context): ViewModel() {
    val pm: PackageManager? = context.packageManager
    private val _apps = MutableStateFlow<List<App>>(emptyList())
    val apps: StateFlow<List<App>> = _apps.asStateFlow()

    private val _pinnedApps = MutableStateFlow<List<App>>(emptyList())
    val pinnedApps: StateFlow<List<App>> = _pinnedApps.asStateFlow()


    val batteryInfo = appsRepository.batteryInfo

    private val _navigateToBlockedAppPage = Channel<App?>()
    val navigateToBlockedAppPage = _navigateToBlockedAppPage.receiveAsFlow()

    private val _navigateToBlockingAppPage = Channel<App?>()
    val navigateToBlockingAppPage = _navigateToBlockingAppPage.receiveAsFlow()

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
        val ftApps = appsRepository.getAllApps()
        if (ftApps.isEmpty()){
            viewModelScope.launch(Dispatchers.IO) {
                loadApps()
            }
        }else{
            _apps.value = ftApps
        }
    }

    fun blockUnblockAppFc(app: App,blocked: Int){
        viewModelScope.launch(Dispatchers.IO) {
            if (app.isPinned == true) {
                appsRepository.pinUnpinApp(app.packageName, 0)
            }
            appsRepository.blockUnblockApp(app.packageName, blocked, blockReleaseDate = "")
            getApps()
        }
    }
    fun pinUnpinApp(app: App, pinned: Int){
        if (app.isBlocked==true){
            Toast.makeText(context,"You cannot pin a blocked app", Toast.LENGTH_LONG).show()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            appsRepository.pinUnpinApp(app.packageName,pinned)
            getPinnedApps()
            getApps()
        }
    }
    fun hideUnhideAppFc(app: App,hidden: Int){
        viewModelScope.launch(Dispatchers.IO) {
            if (app.isPinned == true) {
                appsRepository.pinUnpinApp(app.packageName, 0)
            }
            appsRepository.hideUnhideApp(app.packageName, hidden)
            getApps()
        }
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
    suspend fun loadApps(){
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
            Log.d("package_name","package name=${ap.packageName}")
            if (ap.packageName.contains("camera",true)){
                context.dataStore.edit { st ->
                    st[CAMERA_APP_PACKAGE] = ap.packageName
                }
            }else if (ap.packageName.contains("dialer",true)){
                context.dataStore.edit { st ->
                    st[PHONE_APP_PACKAGE] = ap.packageName
                }
            }else if (ap.packageName.contains("clock",true)){
                context.dataStore.edit { st ->
                    st[CLOCK_APP_PACKAGE] = ap.packageName
                }
            }
            val category = getCategoryName(ap.category)
//            App(packageName = ap.packageName, category = category, name = pm?.getApplicationLabel(ap).toString())
            App(packageName = ap.packageName, category = category, name = ap.loadLabel(pm as PackageManager).toString())
        }
        appsRepository.insertApps(newApps)
        _apps.value = newApps
    }
    private fun getCategoryName(category: Int): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (category) {
                ApplicationInfo.CATEGORY_GAME -> "Game"
                ApplicationInfo.CATEGORY_AUDIO -> "Audio"
                ApplicationInfo.CATEGORY_VIDEO -> "Video"
                ApplicationInfo.CATEGORY_IMAGE -> "Image"
                ApplicationInfo.CATEGORY_SOCIAL -> "Social"
                ApplicationInfo.CATEGORY_NEWS -> "News"
                ApplicationInfo.CATEGORY_MAPS -> "Maps"
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
                ApplicationInfo.CATEGORY_ACCESSIBILITY -> "Accessibility"
                ApplicationInfo.CATEGORY_UNDEFINED -> "Undefined"
                else -> "Other"
            }
        } else {
            "Not Available (API < 26)"
        }
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

     fun launchApp(app: App){
         Log.d("package_name",app.packageName)
         viewModelScope.launch(Dispatchers.IO) {
             if (app.isBlocked==true){
                //navigate to the app blocked page.
                 Log.d("passed_time","app=$app")
                if (app.blockReleaseDate !=null &&isDatePassed(app.blockReleaseDate )){
                    Log.d("passed_time","time is passed")
                    appsRepository.blockUnblockApp(app.packageName,0,null)
                    getApps()
                    val intent = pm?.getLaunchIntentForPackage(app.packageName)
                    if (intent != null){
                        context.startActivity(intent)
                    }else{
                        Toast.makeText(context, "Can't Launch this app", Toast.LENGTH_SHORT).show()
                    }
                }else {
                    val intent = Intent(context, BlockedAppActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("app_name", app.name)
                        putExtra("release_date", app.blockReleaseDate)
                    }
                    context.startActivity(intent)
                }
            }else {
                 val intent = pm?.getLaunchIntentForPackage(app.packageName)
                 if (intent != null) {
                     context.startActivity(intent)
                 } else {
                     Toast.makeText(context, "Can't Launch this app", Toast.LENGTH_SHORT).show()
                 }
             }
        }
    }
}