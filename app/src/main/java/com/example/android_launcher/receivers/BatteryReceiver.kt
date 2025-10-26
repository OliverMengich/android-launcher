package com.example.android_launcher.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import com.example.android_launcher.domain.models.BatteryInfo
import com.example.android_launcher.domain.repository.AppsRepository
import org.koin.core.context.GlobalContext

class BatteryReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val appsRepository = GlobalContext.get().get<AppsRepository>()
        when(intent?.action){
            Intent.ACTION_BATTERY_CHANGED -> {
                Log.d("battery","Battery changed")
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val percentage = level * 100 / scale
                val isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING) || (status == BatteryManager.BATTERY_STATUS_FULL)
                appsRepository.batteryInfo.tryEmit(
                    value= BatteryInfo(isCharging = isCharging, batteryLevel = percentage)
                )
            }
        }
    }
}