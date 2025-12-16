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
import com.example.android_launcher.domain.manager.LocalManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SharedViewModel(private val appsRepository: AppsRepository, private val context: Context): ViewModel() {
    val pm: PackageManager? = context.packageManager

    val localManagerData = context.dataStore.data.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = LocalManager()
    )

    val apps = appsRepository.getAllApps().stateIn(viewModelScope, started=SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), initialValue=emptyList())

    private val _pinnedApps = MutableStateFlow<List<App>>(emptyList())
    val pinnedApps: StateFlow<List<App>> = _pinnedApps.asStateFlow()

    val batteryInfo = appsRepository.batteryInfo
    val refetchAppsFlow = appsRepository.refetchAppsFlow

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
            getPinnedApps()
        }
    }

    fun blockUnblockAppFc(app: App,blocked: Int){
        viewModelScope.launch(Dispatchers.IO) {
            if (app.isPinned == true) {
                appsRepository.pinUnpinApp(app.packageName, 0)
            }
            appsRepository.blockUnblockApp(app.packageName, blocked, blockReleaseDate = "")

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
        }
    }

    fun hideUnhideAppFc(app: App,hidden: Int){
        viewModelScope.launch(Dispatchers.IO) {
            if (app.isPinned == true) {
                appsRepository.pinUnpinApp(app.packageName, 0)
            }
            appsRepository.hideUnhideApp(app.packageName, hidden)
            getHiddenApps()
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