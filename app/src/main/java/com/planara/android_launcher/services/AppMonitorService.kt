package com.planara.android_launcher.services

import android.app.AppOpsManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.planara.android_launcher.BlockedAppActivity
import com.planara.android_launcher.OnboardingActivity
import com.planara.android_launcher.R
import com.planara.android_launcher.dataStore
import com.planara.android_launcher.domain.models.BlockType
import com.planara.android_launcher.domain.repository.AppsRepository
import com.planara.android_launcher.utils.formatIsoTimeToFriendly
import com.planara.android_launcher.utils.isDatePassed
import com.planara.android_launcher.utils.isNowBetween
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.time.LocalDateTime
import java.time.LocalTime

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
                val isLoggedInUser = dataStore.data.map { it.isLoggedIn }.first()
                val hasOverlay = Settings.canDrawOverlays(this@AppMonitorService)
//                val sharedRef = getSharedPreferences("settings_value", Context.MODE_PRIVATE)
//                val isLoggedInUser = sharedRef.getBoolean("IS_AUTHENTICATED",false)
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
//                    val sharedRef = getSharedPreferences("settings_value",Context.MODE_PRIVATE)
                    val focusMode = dataStore.data.map { it.focusMode }.first()
//                    val isFocusModeOn = sharedRef.getBoolean("focus_mode",false)
                    if (focusMode.isActive && !FocusModeService.isRunning){

                        val focusModeEndTime = focusMode.endTime.let {
                            LocalDateTime.parse(it)
                        }

                        if (focusModeEndTime !=null && LocalDateTime.now().isAfter(focusModeEndTime)){
                            dataStore.updateData {
                                it.copy(
                                    focusMode = it.focusMode.copy(
                                        isActive = false,
                                        endTime = ""
                                    )
                                )
                            }
                        }else if(focusModeEndTime != null && LocalDateTime.now().isBefore(focusModeEndTime)){
                            Log.d("focusModeState","restarting focus mode because it was stopped")
                            startForegroundService(Intent(this@AppMonitorService,FocusModeService::class.java))
                        }
                    }

                    val blockedApps = appsRepository.getBlockedApps()

                    val appsPackageNames = blockedApps.map { it.packageName }
                    Log.d("blocked_apps", blockedApps.toString())
                    if (topApp in appsPackageNames) {
                        val ap = blockedApps.find { it.packageName == topApp }
                        if (ap != null){
                            if (ap.releaseDate == null) {
                                appsRepository.blockUnblockApp(ap.packageName, ap.blockType, null)
                                return@launch
                            }else if ( isDatePassed(isDateTime = ap.releaseDate)){
                                appsRepository.blockUnblockApp(ap.packageName,ap.blockType,null)
                                return@launch
                            }
                            else if (ap.blockType.first == BlockType.SCHEDULED){
                                ap.blockType.second.forEach { (startTime, endTime) ->
                                    if (isNowBetween(startTime,endTime)){
                                        return@launch
                                    }
                                }
                                Intent(
                                    this@AppMonitorService,
                                    BlockedAppActivity::class.java
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    putExtra("app_name", ap.name)
                                    putExtra("message", "You block ${ap.name} until ${formatIsoTimeToFriendly(input=ap.releaseDate)}. Digital detox is working, keep moving")
                                }.also {startActivity(it) }
                            }else{
                                val intent = Intent(
                                    this@AppMonitorService,
                                    BlockedAppActivity::class.java
                                ).apply {
                                    putExtra("app_name", ap.name)
                                    putExtra("message", "You block ${ap.name} until ${formatIsoTimeToFriendly(input=ap.releaseDate)}. Digital detox is working, keep moving")
                                }
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }
                        }
                    }
                    val blockedApps1 = dataStore.data.map { it.blockedApps }.first()
                    val appPkgNames = blockedApps1.map { it.packageName }
                    if(topApp in appPkgNames){
                        val ap = blockedApps1.find { it.packageName == topApp }
                        when(ap?.blockType?.first){
                            BlockType.NORMAL->{
                                if (isDatePassed(ap.releaseDate)){
                                    dataStore.updateData {
                                        it.copy(
                                            blockedApps = it.blockedApps.filter { blockedApp ->  blockedApp.packageName != topApp.toString() }
                                        )
                                    }
                                    return@launch
                                }else{
                                    val intent = Intent(
                                        this@AppMonitorService,
                                        BlockedAppActivity::class.java
                                    ).apply {
                                        putExtra("app_name", ap.name)
                                        putExtra("message", "You block ${ap.name} until ${formatIsoTimeToFriendly(input=ap.releaseDate)}. Digital detox is working, keep moving")
                                    }
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                }
                            }
                            BlockType.SCHEDULED->{
                                if (isDatePassed(ap.releaseDate)){
                                    dataStore.updateData {
                                        it.copy(
                                            blockedApps = it.blockedApps.filter { blockedApp ->  blockedApp.packageName != topApp.toString() }
                                        )
                                    }
                                    return@launch
                                }
                                ap.blockType.second.map { (startTime, endTime) ->
                                    if (isNowBetween(startTime,endTime)){
                                        return@launch
                                    }else{
                                        val intent = Intent(
                                            this@AppMonitorService,
                                            BlockedAppActivity::class.java
                                        ).apply {
                                            putExtra("app_name", ap.name)
                                            val msg = ap.blockType.second.joinToString{ (startTime, endTime) -> "$startTime - $endTime" }
                                            putExtra("message", "You block ${ap.name} and you can only use it at $msg until ${formatIsoTimeToFriendly(input=ap.releaseDate)}. Digital detox is working, keep moving")
                                        }
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                    }
                                }
                            }
                            else -> {}
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
            OnboardingActivity.isVisible = true
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
        Log.d("AppMonitorService","stopped monitoring service.")
        handler.removeCallbacks(checkRunnable)
        running = false
        super.onDestroy()
        serviceJob.cancel()
//        val restartIntent = Intent(applicationContext, AppMonitorService::class.java)
//        applicationContext.startForegroundService(restartIntent)
    }

    private fun startForegroundNotification() {
        val channelId = "AppMonitorChannel"
        val channel = NotificationChannel(
            channelId,
            "App Monitor",
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
            description="Used for app monitoring service"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
        val notificationIntent = Intent(this, Class.forName("com.planara.android_launcher.MainActivity"))
        val pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this,channelId)
            .setSmallIcon(R.drawable.planara_icon)
            .setContentTitle("Monitoring apps.")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
//        val notification = Notification.Builder(this, channelId)
//            .setSmallIcon(R.drawable.planara_icon)
//            .setContentTitle("Monitoring apps")
//            .setSmallIcon(android.R.drawable.ic_menu_info_details)
//            .setSilent(true)
//            .setOngoing(true)
//            .build()
        startForeground(1, notification)
    }
    private fun getTopApp(): String?{
        val now = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now- 10_000,now)
        return stats?.maxByOrNull { it.lastTimeUsed }?.packageName
    }
}