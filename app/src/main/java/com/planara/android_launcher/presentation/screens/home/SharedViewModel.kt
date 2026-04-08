package com.planara.android_launcher.presentation.screens.home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planara.android_launcher.BlockedAppActivity
import com.planara.android_launcher.dataStore
import com.planara.android_launcher.domain.models.App
import com.planara.android_launcher.domain.repository.AppsRepository
import com.planara.android_launcher.utils.isDatePassed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.planara.android_launcher.domain.manager.LocalManager
import com.planara.android_launcher.domain.models.BlockType
import com.planara.android_launcher.domain.models.UsageTime
import com.planara.android_launcher.utils.formatIsoTimeToFriendly
import com.planara.android_launcher.utils.isNowBetween
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

class SharedViewModel(private val appsRepository: AppsRepository, private val context: Context): ViewModel() {
    val pm: PackageManager? = context.packageManager

    val localManagerData = context.dataStore.data.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = LocalManager()
    )

    val apps = appsRepository.getAllApps().stateIn(viewModelScope, started=SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), initialValue=emptyList())
    val pinnedApps = appsRepository.getPinnedApps().stateIn(viewModelScope,started=SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), initialValue=emptyList())

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


    fun blockUnblockAppFc(app: App,blockType:  Pair<BlockType, List<UsageTime>>){
        viewModelScope.launch(Dispatchers.IO) {
            val isAppBlocked = context.dataStore.data.first().pinnedApps.find { it==app.packageName }
            if (isAppBlocked !=null) {
                appsRepository.pinUnpinApp(app.packageName, false)
            }
            appsRepository.blockUnblockApp(app.packageName, blockType, blockReleaseDate = "")
        }
    }
    fun pinUnpinApp(app: App){
        viewModelScope.launch(Dispatchers.IO) {
            val isAppPinned = context.dataStore.data.first().pinnedApps.find { it == app.packageName }
            appsRepository.pinUnpinApp(app.packageName, pin = isAppPinned == null)
        }
    }

    fun hideUnhideAppFc(app: App,hidden: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            if (app.isPinned == true) {
                appsRepository.pinUnpinApp(app.packageName, false)
            }
            appsRepository.hideUnhideApp(app.packageName, hidden)
            getHiddenApps()
        }
    }

     fun launchApp(app: App,callBackFunction: (()->Unit)?=null){
         Log.d("package_name",app.packageName)
         viewModelScope.launch(Dispatchers.IO) {
             val blockedApps = appsRepository.getBlockedApps()
             val ap = blockedApps.find { it.packageName == app.packageName }
             if (ap != null){
                 if (ap.releaseDate == null){
                     appsRepository.blockUnblockApp(app.packageName,ap.blockType, blockReleaseDate = null)
                     val intent = pm?.getLaunchIntentForPackage(app.packageName).also {
                         it?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                     }
                     if (intent != null) {
                         context.startActivity(intent)
                     } else {
                         Toast.makeText(context, "Can't Launch this app", Toast.LENGTH_SHORT).show()
                     }
                 }else if (isDatePassed(isDateTime = ap.releaseDate)){
                     appsRepository.blockUnblockApp(app.packageName,ap.blockType,null)
                     val intent = pm?.getLaunchIntentForPackage(app.packageName).also {
                         it?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                     }
                     if (intent != null) {
                         context.startActivity(intent)
                     } else {
                         Toast.makeText(context, "Can't Launch this app", Toast.LENGTH_SHORT).show()
                     }
                 }
                 else if (ap.blockType.first == BlockType.SCHEDULED){
                     ap.blockType.second.forEach { (startTime, endTime) ->
                         if (isNowBetween(startTime,endTime)){
                             val intent = pm?.getLaunchIntentForPackage(app.packageName).also {
                                 it?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                             }
                             if (intent != null) {
                                 context.startActivity(intent)
                             } else {
                                 Toast.makeText(context, "Can't Launch this app", Toast.LENGTH_SHORT).show()
                             }
                             return@launch
                         }
                     }
                     Intent(context, BlockedAppActivity::class.java).apply {
                         addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                         putExtra("app_name", app.name)
                         val msg = ap.blockType.second.joinToString{ (startTime, endTime) -> "$startTime - $endTime" }
                         putExtra("message", "You block ${app.name} and you can only use it at $msg until ${formatIsoTimeToFriendly(input=ap.releaseDate)}. Digital detox is working, keep moving")
                     }.also { context.startActivity(it) }
                 }else{
                     Intent(context, BlockedAppActivity::class.java).apply {
                         addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                         putExtra("app_name", app.name)
                         putExtra("message", "You block ${app.name} until ${formatIsoTimeToFriendly(input=ap.releaseDate)}. Digital detox is working, keep moving")
                     }.also { context.startActivity(it) }
                 }
//                if (ap.releaseDate != null && isDatePassed(isDateTime = ap.releaseDate)){
//                    val blockType = ap.blockType
//                    appsRepository.blockUnblockApp(app.packageName,blockType,null)
//                    val intent = pm?.getLaunchIntentForPackage(app.packageName)
//                    if (intent != null){
//                        context.startActivity(intent)
//                    }else{
//                        Toast.makeText(context, "Can't Launch this app", Toast.LENGTH_SHORT).show()
//                    }
//                }else {
//                    Intent(context, BlockedAppActivity::class.java).apply {
//                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        putExtra("app_name", app.name)
//                        putExtra("release_date", ap.releaseDate)
//                    }.also { context.startActivity(it) }
//                }
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