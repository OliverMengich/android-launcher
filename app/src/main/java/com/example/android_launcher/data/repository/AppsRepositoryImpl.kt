package com.example.android_launcher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.util.Log
import androidx.datastore.preferences.core.edit
import com.example.android_launcher.CAMERA_APP_PACKAGE
import com.example.android_launcher.CLOCK_APP_PACKAGE
import com.example.android_launcher.PHONE_APP_PACKAGE
import com.example.android_launcher.data.local.AppsDao
import com.example.android_launcher.dataStore
import com.example.android_launcher.domain.models.App
import com.example.android_launcher.domain.models.BatteryInfo
import com.example.android_launcher.domain.repository.AppsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

class AppsRepositoryImpl(private val appsDao: AppsDao, private val context: Context): AppsRepository {

    private val pm = context.packageManager
    override val batteryInfo = MutableStateFlow<BatteryInfo?>(null)
    override val refetchAppsFlow= MutableStateFlow<Boolean?> (false)

    override fun getAllApps(): Flow<List<App>> {
        return appsDao.getAllApps()
            .onEach { apps ->
                if (apps.isEmpty()) {
                    loadApps()
                }
            }
    }

    private suspend fun loadApps() = withContext(Dispatchers.IO){
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
                defaultCameraPkg-> context.dataStore.updateData { it.copy(cameraApp = packageName) }
                defaultDialerPkg-> context.dataStore.updateData { it.copy(phoneApp = packageName) }
                defaultClockPkg-> context.dataStore.updateData { it.copy(clockApp = packageName) }
            }
            val appDomains = getDomainsFromApp(label)
            Log.d("app_domains",appDomains.toString())
            App(packageName = packageName, category = category, domains = appDomains, name = label)
        }?.distinctBy { it.packageName }?.sortedBy { it.name.lowercase() }
        appsDao.insertApps(apps = appsList?:emptyList())
    }
    private fun getDomainsFromApp(appName: String): List<String> {
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
    override fun batteryInfoReceiver(batteryInfoValue: BatteryInfo) {
        batteryInfo.value = batteryInfoValue
    }

    override fun insertApps(apps: List<App>) {
        appsDao.insertApps(
            apps = apps
        )
    }

    override fun getPinnedApps(): Flow<List<App>> {
        return appsDao.getPinnedApps()
    }

    override fun getHiddenApps(): List<App> {
        return appsDao.getHiddenApps()
    }

    override fun getBlockedApps(): List<App> {
        return appsDao.getBlockedApps()
    }

    override suspend fun blockUnblockApp(
        packageName: String,
        blocked: Int,
        blockReleaseDate: String?
    ) {
        appsDao.blockUnblockApp(packageName,blocked, blockReleaseDate = blockReleaseDate)
    }

    override suspend fun pinUnpinApp(packageName: String,pinned: Int) {
        appsDao.pinApp(packageName,pinned)
    }

    override suspend fun hideUnhideApp(packageName: String, hidden: Int) {
        appsDao.hideUnhideApp(packageName,hidden)
    }

    override fun newAppInstalled(app: App) {
        appsDao.insertApp(app)
    }

    override suspend fun removeUninstalled(packageName: String) {
        appsDao.deleteApp(packageName)
    }
}