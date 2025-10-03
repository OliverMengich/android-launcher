package com.example.android_launcher.presentation.screens.home

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.Uri
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
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

class SharedViewModel(private val appsRepository: AppsRepository, private val context: Context): ViewModel() {
    val pm: PackageManager? = context.packageManager
    private val _apps = MutableStateFlow<List<App>>(emptyList())
    val apps: StateFlow<List<App>> = _apps.asStateFlow()

    private val _pinnedApps = MutableStateFlow<List<App>>(emptyList())
    val pinnedApps: StateFlow<List<App>> = _pinnedApps.asStateFlow()

    val batteryInfo = appsRepository.batteryInfo
    val refetchAppsFlow = appsRepository.refetchAppsFlow
    init {
        viewModelScope.launch {
            appsRepository.refetchAppsFlow.collect { rf->
                if (rf==true){
                    getApps()
                }
            }
        }
    }

    private val _navigateToBlockedAppPage = Channel<App?>()
    val navigateToBlockedAppPage = _navigateToBlockedAppPage.receiveAsFlow()

    private val _navigateToBlockingAppPage = Channel<App?>()
    val navigateToBlockingAppPage = _navigateToBlockingAppPage.receiveAsFlow()


    private val _hiddenApps = MutableStateFlow<List<App>>(emptyList())
    val hiddenApps: StateFlow<List<App>> = _hiddenApps.asStateFlow()

    fun getHiddenApps(){
        viewModelScope.launch(Dispatchers.IO) {
            val hdApps = appsRepository.getHiddenApps()
            Log.d("hidden_apps",hdApps.toString())
            _hiddenApps.value = hdApps
        }
    }

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
        viewModelScope.launch(Dispatchers.IO) {
            val ftApps = appsRepository.getAllApps()
            if (ftApps.isEmpty()){
                loadApps()
            }else{
                _apps.value = ftApps
            }
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
            getHiddenApps()
            getApps()
        }
    }

    fun getDomainsFromApp(appName: String): List<String> {
        val normalized = appName
            .trim()
            .lowercase()
            .replace(" ", "")
            .replace("_", "")
            .replace("-", "")

        val domains = mutableSetOf<String>()

        domains.add("$normalized.com")
        domains.add("www.$normalized.com")
        domains.add("m.$normalized.com")

        when (normalized) {
            "youtube" -> domains.addAll(listOf("youtu.be"))
            "x", "twitter" -> domains.addAll(listOf("twitter.com", "mobile.twitter.com"))
            "facebook" -> domains.addAll(listOf("fb.com"))
            "tiktok" -> domains.addAll(listOf("vm.tiktok.com"))
            "snapchat" -> domains.addAll(listOf("web.snapchat.com"))
            "reddit" -> domains.addAll(listOf("old.reddit.com", "new.reddit.com"))
            "telegram" -> domains.addAll(listOf("t.me"))
            "whatsapp" -> domains.addAll(listOf("wa.me", "web.whatsapp.com"))
        }

        return domains.toList()
    }
    suspend fun loadApps() = withContext(Dispatchers.IO){
        val defaultCameraPkg = pm?.resolveActivity(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE),
            PackageManager.MATCH_DEFAULT_ONLY
        )?.activityInfo?.packageName
        val defaultDialerPkg = pm?.resolveActivity(
            Intent(Intent.ACTION_DIAL),
            PackageManager.MATCH_DEFAULT_ONLY
        )?.activityInfo?.packageName
        val defaultClockPkg = pm?.resolveActivity(
            Intent(AlarmClock.ACTION_SHOW_ALARMS),
            PackageManager.MATCH_DEFAULT_ONLY
        )?.activityInfo?.packageName
        val launchIntent = Intent(Intent.ACTION_MAIN,null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm?.queryIntentActivities(launchIntent,0)
        val appsList = resolveInfos?.mapNotNull { info->
            if(info.activityInfo.packageName == context.packageName){
                return@mapNotNull null
            }
            val appInfo = info.activityInfo.applicationInfo
            val label = info.loadLabel(pm).toString()
            val packageName = info.activityInfo.packageName
            val category = getCategoryName(appInfo.category)
            when(packageName){
                defaultCameraPkg-> context.dataStore.edit { it[CAMERA_APP_PACKAGE]=packageName }
                defaultDialerPkg-> context.dataStore.edit { it[PHONE_APP_PACKAGE]=packageName }
                defaultClockPkg-> context.dataStore.edit { it[CLOCK_APP_PACKAGE]=packageName }
            }
            val appDomains = getDomainsFromApp(label)
            Log.d("app_domains",appDomains.toString())
            App(packageName = packageName, category = category, domains = appDomains, name = label)
        }?.distinctBy { it.packageName }?.sortedBy { it.name.lowercase() }
        appsRepository.insertApps(apps=appsList ?: emptyList())
        _apps.value = appsList ?: emptyList()
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

     fun launchApp(app: App,callBackFunction: (()->Unit)?=null){
         Log.d("package_name",app.packageName)
         viewModelScope.launch(Dispatchers.IO) {
             if (app.isBlocked==true){
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
                    Intent(context, BlockedAppActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("app_name", app.name)
                        putExtra("release_date", app.blockReleaseDate)
                    }.also { context.startActivity(it) }
                }
            }else {
                 val intent = pm?.getLaunchIntentForPackage(app.packageName).also {
                     it?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                     it?.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                     it?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                 }
                 if (intent != null) {
                     context.startActivity(intent)
                 } else {
                     Toast.makeText(context, "Can't Launch this app", Toast.LENGTH_SHORT).show()
                 }
            }
             callBackFunction?.invoke()
        }
    }
}