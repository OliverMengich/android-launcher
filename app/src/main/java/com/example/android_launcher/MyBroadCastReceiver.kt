package com.example.android_launcher

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.util.Log
import com.example.android_launcher.domain.models.App
import com.example.android_launcher.domain.models.BatteryInfo
import com.example.android_launcher.domain.repository.AppsRepository
import com.example.android_launcher.presentation.screens.home.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext
import javax.inject.Inject
import kotlin.random.Random


class MyBroadCastReceiver: BroadcastReceiver() {
    @SuppressLint("SuspiciousIndentation")
    override fun onReceive(context: Context?, intent: Intent?) {
        val appsRepository = GlobalContext.get().get<AppsRepository>()
        print("Received")
        val pm = context?.packageManager
        when(intent?.action){
            Intent.ACTION_BATTERY_CHANGED->{
                Log.d("battery","Battery changed")
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val percentage = level * 100 / scale
                val isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING) || (status == BatteryManager.BATTERY_STATUS_FULL)

                appsRepository.batteryInfo.tryEmit(
                    BatteryInfo(isCharging = isCharging, batteryLevel = percentage)
                )
//                appsRepository.batteryInfo.value = BatteryInfo(isCharging = isCharging, batteryLevel = percentage)
                Log.d("BatteryReceiver", "Battery level: $percentage% $isCharging")
//                sharedViewModel.batteryLevelHandler(percentage)
                //send event to viewmodel to show current power level.
            }
            Intent.ACTION_PACKAGE_ADDED->{
                Log.d("new_app","new app installed")
                val packageName = intent.data?.encodedSchemeSpecificPart ?: return
                val replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                if (replacing) return // ignore if it's an app update
                context?.let {
                    try {
                        val appInfo = pm?.getApplicationInfo(packageName,0) ?: return
                        val appLabel = pm.getApplicationLabel(appInfo).toString()

                        appsRepository.newAppInstalled(
                            app = App(packageName = packageName, name = appLabel)
                        )
                    }catch (e: PackageManager.NameNotFoundException){
                        Log.e("AppInstall","App not found")
                    }
                }
            }
            Intent.ACTION_PACKAGE_REMOVED->{
                val packageName = intent.data?.encodedSchemeSpecificPart ?: return
                val replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                if (replacing) return // ignore if it's an app update
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        appsRepository.removeUninstalled(packageName)
                    }finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}