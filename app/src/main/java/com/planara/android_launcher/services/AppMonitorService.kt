package com.planara.android_launcher.services

import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.planara.android_launcher.BlockedAppActivity
import com.planara.android_launcher.OnboardingActivity
import com.planara.android_launcher.R
import com.planara.android_launcher.dataStore
import com.planara.android_launcher.domain.models.BlockType
import com.planara.android_launcher.domain.repository.AppsRepository
import com.planara.android_launcher.utils.formatIsoTimeToFriendly
import com.planara.android_launcher.utils.describe
import com.planara.android_launcher.utils.isDatePassed
import com.planara.android_launcher.utils.isExpired
import com.planara.android_launcher.utils.isNowBetween
import com.planara.android_launcher.utils.joinToStringWithAnd
import com.planara.android_launcher.utils.mostRelevantActive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AppMonitorService : Service() {

    private val appsRepository: AppsRepository by inject()
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var appOpsManager: AppOpsManager
    private lateinit var powerManager: PowerManager

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var monitorJob: Job? = null

    private val opListener = AppOpsManager.OnOpChangedListener { op, pkg ->
        if (op == AppOpsManager.OPSTR_GET_USAGE_STATS && pkg == packageName) {
            if (!hasUsageAccess(this@AppMonitorService)) {
                handlePermissionRevoked()
            }
        }
    }

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> startMonitoringLoop()
                Intent.ACTION_SCREEN_OFF -> stopMonitoringLoop()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        appOpsManager.startWatchingMode(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            packageName,
            opListener
        )

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, filter)

        startForegroundNotification()

        if (powerManager.isInteractive) {
            startMonitoringLoop()
        }
    }

    private fun startMonitoringLoop() {
        if (monitorJob?.isActive == true) return

        monitorJob = serviceScope.launch {
            while (isActive) {
                try {
                    performCheck()
                } catch (e: Exception) {
                    Log.e("AppMonitorService", "Error during monitoring cycle", e)
                }
                delay(2000)
            }
        }
    }

    private fun stopMonitoringLoop() {
        monitorJob?.cancel()
        monitorJob = null
    }

    private suspend fun performCheck() {
        val isLoggedInUser = dataStore.data.map { it.isLoggedIn }.first()
        val hasOverlay = Settings.canDrawOverlays(this@AppMonitorService)
        val hasUsage = hasUsageAccess(this@AppMonitorService)

        if (!hasOverlay && isLoggedInUser) {
            launchRegrantActivity(2)
            stopSelf()
            return
        }
        if (!hasUsage && isLoggedInUser) {
            handlePermissionRevoked()
            launchRegrantActivity(1)
            return
        }

        if (!isLoggedInUser) return

        val topApp = getTopApp()

        // Update Active Tab cleanly
        if (topApp != null && topApp != packageName) {
            dataStore.updateData { currentPreferences ->
                if (currentPreferences.activeTab != 1) {
                    currentPreferences.copy(activeTab = 1)
                } else {
                    currentPreferences
                }
            }
        }

        val localManager = dataStore.data.first()

        // A user may have several focus sessions enabled at once (e.g. a long one until the end
        // date plus a short "today 10am - 1pm" one). Prune expired ones and enforce if any is in effect.
        val focusModes = localManager.allFocusModes()
        val liveFocusModes = focusModes.filter { !it.isExpired() }
        if (liveFocusModes.size != focusModes.size) {
            dataStore.updateData { prefs ->
                prefs.copy(
                    focusMode = prefs.focusMode?.takeIf { !it.isExpired() },
                    focusModes = prefs.focusModes.filter { !it.isExpired() }
                )
            }
        }

        // Handle Focus Mode
        if (liveFocusModes.isNotEmpty() && topApp != null && topApp != packageName) {
            val enforced = liveFocusModes.mostRelevantActive()
            if (enforced != null) {
                val until = enforced.endDate.ifBlank() { liveFocusModes.map { it.endDate }.filter { it.isNotBlank() }.minOrNull().orEmpty() }
                Intent(this@AppMonitorService, BlockedAppActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("app_name", "All apps")
                    putExtra(
                        "message",
                        buildString {
                            append("Focus mode is currently active (${enforced.describe()}).")
                            if (until.isNotBlank()) append(" Focus ends ${formatIsoTimeToFriendly(input = until)}.")
                            append(" Digital detox is working, keep moving.")
                        }
                    )
                }.also { startActivity(it) }
                return
            }
        }

        if (topApp == null || topApp == packageName) return

        // Handle Blocked Apps via Repository
        val blockedApps = appsRepository.getBlockedApps()
        val appsPackageNames = blockedApps.map { it.packageName }

        if (topApp in appsPackageNames) {
            val ap = blockedApps.find { it.packageName == topApp }
            if (ap != null) {
                if (ap.releaseDate == null || isDatePassed(isDateTime = ap.releaseDate)) {
                    appsRepository.blockUnblockApp(ap.packageName, ap.blockType, null)
                    return
                }

                if (ap.blockType.first == BlockType.USE_BETWEEN) {
                    val allowed = ap.blockType.second.any { (startTime, endTime) ->
                        isNowBetween(startTime, endTime)
                    }
                    if (!allowed) {
                        Intent(this@AppMonitorService, BlockedAppActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            putExtra("app_name", ap.name)
                            val msg = ap.blockType.second.joinToStringWithAnd { (startTime, endTime) -> "$startTime - $endTime" }
                            putExtra("message", "You block ${ap.name} and you cannot use it at $msg until ${formatIsoTimeToFriendly(input = ap.releaseDate)}. Digital detox is working, keep moving.")
                        }.also { startActivity(it) }
                        return
                    }
                } else if (ap.blockType.first == BlockType.NOT_USE) {
                    val restricted = ap.blockType.second.any { (startTime, endTime) ->
                        isNowBetween(startTime, endTime)
                    }
                    if (restricted) {
                        Intent(this@AppMonitorService, BlockedAppActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            putExtra("app_name", ap.name)
                            val msg = ap.blockType.second.joinToStringWithAnd { (startTime, endTime) -> "$startTime - $endTime" }
                            putExtra("message", "You block ${ap.name} and you cannot use it at $msg until ${formatIsoTimeToFriendly(input = ap.releaseDate)}. Digital detox is working, keep moving.")
                        }.also { startActivity(it) }
                        return
                    }
                } else {
                    Intent(this@AppMonitorService, BlockedAppActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        putExtra("app_name", ap.name)
                        putExtra("message", "You block ${ap.name} until ${formatIsoTimeToFriendly(input = ap.releaseDate)}. Digital detox is working, keep moving")
                    }.also { startActivity(it) }
                    return
                }
            }
        }

        // Handle DataStore Blocked Apps
        val blockedApps1 = dataStore.data.map { it.blockedApps }.first()
        val appPkgNames = blockedApps1.map { it.packageName }

        if (topApp in appPkgNames) {
            val ap = blockedApps1.find { it.packageName == topApp }
            when (ap?.blockType?.first) {
                BlockType.NORMAL -> {
                    if (isDatePassed(ap.releaseDate)) {
                        dataStore.updateData {
                            it.copy(blockedApps = it.blockedApps.filter { blockedApp -> blockedApp.packageName != topApp })
                        }
                        return
                    } else {
                        Intent(this@AppMonitorService, BlockedAppActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            putExtra("app_name", ap.name)
                            putExtra("message", "You block ${ap.name} until ${formatIsoTimeToFriendly(input = ap.releaseDate)}. Digital detox is working, keep moving")
                        }.also { startActivity(it) }
                    }
                }
                BlockType.USE_BETWEEN -> {
                    if (isDatePassed(ap.releaseDate)) {
                        dataStore.updateData {
                            it.copy(blockedApps = it.blockedApps.filter { blockedApp -> blockedApp.packageName != topApp })
                        }
                        return
                    }
                    val allowed = ap.blockType.second.any { (startTime, endTime) ->
                        isNowBetween(startTime, endTime)
                    }
                    if (!allowed) {
                        Intent(this@AppMonitorService, BlockedAppActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            putExtra("app_name", ap.name)
                            val msg = ap.blockType.second.joinToString { (startTime, endTime) -> "$startTime - $endTime" }
                            putExtra("message", "You block ${ap.name} and you can only use it at $msg until ${formatIsoTimeToFriendly(input = ap.releaseDate)}. Digital detox is working, keep moving")
                        }.also { startActivity(it) }
                    }
                }
                BlockType.NOT_USE -> {
                    if (isDatePassed(ap.releaseDate)) {
                        dataStore.updateData {
                            it.copy(blockedApps = it.blockedApps.filter { blockedApp -> blockedApp.packageName != topApp })
                        }
                        return
                    }
                    val restricted = ap.blockType.second.any { (startTime, endTime) ->
                        isNowBetween(startTime, endTime)
                    }
                    if (restricted) {
                        Intent(this@AppMonitorService, BlockedAppActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            putExtra("app_name", ap.name)
                            val msg = ap.blockType.second.joinToString { (startTime, endTime) -> "$startTime - $endTime" }
                            putExtra("message", "You block ${ap.name} and you cannot use it at $msg until ${formatIsoTimeToFriendly(input = ap.releaseDate)}. Digital detox is working, keep moving.")
                        }.also { startActivity(it) }
                    }
                }
                else -> {}
            }
        }
    }

    private fun handlePermissionRevoked() {
        stopMonitoringLoop()
        serviceScope.launch {
            dataStore.updateData { it.copy(activeTab = 0) }
        }
    }

    private fun launchRegrantActivity(index: Int) {
        if (!OnboardingActivity.isVisible) {
            Intent(this@AppMonitorService, OnboardingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("active_page", index)
            }.also { startActivity(it) }
            OnboardingActivity.isVisible = true
        }
    }

    fun hasUsageAccess(context: Context): Boolean {
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun getTopApp(): String? {
        val now = System.currentTimeMillis()
        // Kept at 10 seconds to ensure OEM-delayed usage events are caught
        val events = usageStatsManager.queryEvents(now - 10_000, now)
        val event = UsageEvents.Event()

        var currentTopApp: String? = null
        var lastEventTime = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    if (event.timeStamp >= lastEventTime) {
                        lastEventTime = event.timeStamp
                        currentTopApp = event.packageName
                    }
                }
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.ACTIVITY_STOPPED -> {
                    if (event.packageName == currentTopApp && event.timeStamp >= lastEventTime) {
                        lastEventTime = event.timeStamp
                        currentTopApp = null
                    }
                }
            }
        }
        return currentTopApp
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
            description = "Used for app monitoring service"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notificationIntent = Intent(this, Class.forName("com.planara.android_launcher.MainActivity"))
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.planara_icon)
            .setContentTitle("Monitoring apps.")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(screenStateReceiver)
        } catch (_: Exception) {}
        appOpsManager.stopWatchingMode(opListener)
        stopMonitoringLoop()
        serviceJob.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }
}