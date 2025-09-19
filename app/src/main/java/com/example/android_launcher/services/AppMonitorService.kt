package com.example.android_launcher.services

import android.app.AppOpsManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import com.example.android_launcher.BlockedAppActivity
import com.example.android_launcher.OnboardingActivity
import com.example.android_launcher.domain.repository.AppsRepository
import com.example.android_launcher.utils.isDatePassed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class AppMonitorService: Service() {

    private lateinit var usageStatsManager: UsageStatsManager
    private val handler = Handler(Looper.getMainLooper())

    private var running = true

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO+serviceJob)

    private val checkRunnable = object : Runnable {
        override fun run() {
            serviceScope.launch {
//                val isLoggedIn = ctxDataStore.data.map { it[IS_LOGGED_IN_KEY] ?: false }.first()
                val hasOverlay = Settings.canDrawOverlays(this@AppMonitorService)
                val sharedRef = getSharedPreferences("settings_value", Context.MODE_PRIVATE)
                val isLoggedInUser = sharedRef.getBoolean("IS_AUTHENTICATED",false)
                Log.d("tag_here", "isLogged = $isLoggedInUser. has overlay= $hasOverlay has usage= ${hasUsageAccess(this@AppMonitorService)}")

                if (hasOverlay && !isLoggedInUser){
                    launchRegrantActivity(4)
                    stopSelf()
                }
                if(hasUsageAccess(context=this@AppMonitorService) && !isLoggedInUser){
                    launchRegrantActivity(3)
                    stopSelf()
//                    launchRegrantUsageActivity()
                }
                if (!hasOverlay && isLoggedInUser){
                    launchRegrantActivity(3)
                    stopSelf()
//                    break
                }
                if(!hasUsageAccess(context=this@AppMonitorService) && isLoggedInUser){
                    launchRegrantActivity(2)
//                    stopSelf()
//                    launchRegrantUsageActivity()
                }
                if(isLoggedInUser) {
                    val topApp = getTopApp()
                    val appsRepository = GlobalContext.get().get<AppsRepository>()

                    val blockedApps = appsRepository.getBlockedApps()

                    val appsPackageNames = blockedApps.map { it.packageName }
                    Log.d("blocked_apps", blockedApps.toString())
                    if (topApp in appsPackageNames) {
                        val ap = blockedApps.find { it.packageName == topApp }
                        if (ap?.blockReleaseDate != null && isDatePassed(ap.blockReleaseDate)) {
                            Log.d("passed", "time is passed")
                            appsRepository.blockUnblockApp(topApp.toString(), 0, null)
                            return@launch
                        } else {
                            // Redirect to your launcher or block screen
                            val intent = Intent(
                                this@AppMonitorService,
                                BlockedAppActivity::class.java
                            ).apply {
                                putExtra("app_name", ap?.name)
                                putExtra("release_date", ap?.blockReleaseDate)
                            }
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    }
                }
            }
            handler.postDelayed(this, 2000) // Repeat every 2 seconds
        }
    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Thread {
//            while (running){
//                val hasOverlay = Settings.canDrawOverlays(this)
//                val sharedRef = getSharedPreferences("settings_value", Context.MODE_PRIVATE)
//                val isLoggedInUser = sharedRef.getBoolean("IS_AUTHENTICATED",false)
//                if (!hasOverlay && isLoggedInUser){
//                    launchRegrantActivity(1)
//                    stopSelf()
//                    break
//                }
//                Thread.sleep(2000)
//            }
//        }.start()
//        return START_STICKY
////        return super.onStartCommand(intent, flags, startId)
//    }
    private fun launchRegrantActivity(index: Int){
        Log.d("tag_here", "is visible= ${OnboardingActivity.isVisible}")
        if(!OnboardingActivity.isVisible) {
            Log.d("tag_here", "is visible= ${OnboardingActivity.isVisible}")
            val intent = Intent(this@AppMonitorService, OnboardingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("active_page", index)
            }
            startActivity(intent)
        }
    }
    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
    override fun onBind(p0: Intent?): IBinder? =null

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        handler.post(checkRunnable)
        startForegroundNotification()
    }

    override fun onDestroy() {
        handler.removeCallbacks(checkRunnable)
        running = false
        super.onDestroy()
        serviceJob.cancel()
    }


    private fun startForegroundNotification() {
        val channelId = "AppMonitorChannel"
        val channel = NotificationChannel(
            channelId,
            "App Monitor",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Monitoring apps")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()

        startForeground(1, notification)
    }
    private fun getTopApp(): String?{
        val now = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now- 10_000,now)
        return stats?.maxByOrNull { it.lastTimeUsed }?.packageName
    }
}