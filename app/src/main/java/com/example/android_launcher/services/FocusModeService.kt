package com.example.android_launcher.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.android_launcher.BlockedAppActivity
import com.example.android_launcher.MainActivity
import com.example.android_launcher.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime

class FocusModeService: LifecycleService() {
    private lateinit var usageStatsManager: UsageStatsManager
    private var lastEventTime = 0L
    private val channelId= "focus_mode_service"
    companion object{
        var isRunning = false
    }
    override fun onCreate() {
        super.onCreate()
        isRunning = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Focus Mode",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description="Notifications for Focus mode."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = createForegroundNotification()

        startForeground(1,notification)
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        observeFocusMode()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }
    private fun createForegroundNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Focus Mode Active")
            .setContentText("Focus Mode is currently running in the background.")
            .setSmallIcon(R.drawable.planara_icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    private fun observeFocusMode(){
//        lifecycleScope.launch {
//            Toast.makeText(this@FocusModeService,"Focus Mode Started",Toast.LENGTH_SHORT).show()
//            val sharedRef = getSharedPreferences("settings_value",MODE_PRIVATE)
//            val isFocusModeOn = sharedRef.getBoolean("focus_mode",false)
//            val focusModeEndTime = sharedRef.getString("focus_mode_end_time","")?.takeIf { it.isNotBlank() }?.let {
//                LocalDateTime.parse(it)
//            }
//            while (true){
//
//                Log.d("focusModeState"," focus time $focusModeEndTime, is on $isFocusModeOn")
//                if(!isFocusModeOn || focusModeEndTime == null){
//                    sharedRef.edit {
//                        putBoolean("focus_mode", false)
//                        putString("focus_mode_end_time", "")
//                        apply()
//                    }
//                    stopSelf()
//                    break
//                }
//                Log.d("focusModeState"," focus time $focusModeEndTime, is on $isFocusModeOn")
//                if (LocalDateTime.now().isAfter(focusModeEndTime)){
//                    Toast.makeText(this@FocusModeService,"Focus Mode Ended",Toast.LENGTH_SHORT).show()
//                    sharedRef.edit {
//                        putBoolean("focus_mode", false)
//                        putString("focus_mode_end_time", "")
//                        apply()
//                    }
//                    stopSelf()
//                    break
//                }
//                Log.d("focusModeState"," focus time $focusModeEndTime, is on $isFocusModeOn")
//                listenForAppLaunches(focusModeEndTime)
//                delay(2000)
//            }
//        }
    }
    private fun listenForAppLaunches(focusModeEndDateTime: LocalDateTime?){
        val now = System.currentTimeMillis()
        val usageEvents = usageStatsManager.queryEvents(lastEventTime,now)
        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()){
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED){
                launchBlockedActivity(focusModeEndDateTime)
            }
        }
        lastEventTime = now
    }
    private fun launchBlockedActivity(focusModeEndDateTime: LocalDateTime?){
        val blockedActivity = Intent(this, BlockedAppActivity::class.java)
        blockedActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        blockedActivity.putExtra("focus_mode",true)
        blockedActivity.putExtra("focus_mode_end_time",focusModeEndDateTime)
        startActivity(blockedActivity)
    }
}