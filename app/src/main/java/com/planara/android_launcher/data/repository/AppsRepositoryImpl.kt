package com.planara.android_launcher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.planara.android_launcher.dataStore
import com.planara.android_launcher.domain.models.App
import com.planara.android_launcher.domain.models.BatteryInfo
import com.planara.android_launcher.domain.models.BlockType
import com.planara.android_launcher.domain.models.BlockedApp
import com.planara.android_launcher.domain.models.UsageTime
import com.planara.android_launcher.domain.repository.AppsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class AppsRepositoryImpl( private val context: Context): AppsRepository {

    private val pm = context.packageManager
    override val batteryInfo = MutableStateFlow<BatteryInfo?>(null)
    override val refetchAppsFlow = MutableStateFlow<Boolean?> (false)
    private val _deviceApps = MutableStateFlow<List<App>>(emptyList())

    override fun getAllApps(): Flow<List<App>> =
        combine(
            flow = _deviceApps,
            flow2 = context.dataStore.data.map { it.hiddenApps }
        ) { apps, hiddenApps ->
            if (apps.isEmpty()) {
                loadApps()
                emptyList()
            } else {
                apps.filter { it.packageName !in hiddenApps }
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
        val appData = context.dataStore.data.first()
        _deviceApps.value = appsList?.map { app ->
            app.copy(
                isHidden = app.packageName in appData.hiddenApps,
                isPinned = app.packageName in  appData.pinnedApps
            )
        }?:emptyList()
        _deviceApps.value = appsList?:emptyList()
        //appsDao.insertApps(apps = appsList?:emptyList())
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
        return when (category) {
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
    }
    override fun batteryInfoReceiver(batteryInfoValue: BatteryInfo) {
        batteryInfo.value = batteryInfoValue
    }

    override fun insertApps(apps: List<App>) {
        _deviceApps.update { apsL->
            val existPkg = apsL.map { it.packageName }.toSet()
            val filteredNew = apps.filter { it.packageName !in existPkg }
            apsL + filteredNew
        }
    }

    override fun getPinnedApps(): Flow<List<App>> = combine(
        flow = _deviceApps,
        flow2 = context.dataStore.data.map { it.pinnedApps }
    ) { apps, pinnedApps ->
        apps.filter { it.packageName in pinnedApps }
    }

    override suspend fun getHiddenApps(): List<App> {
        val hiddenApps = context.dataStore.data.first().hiddenApps
        return _deviceApps.first().filter {
            it.packageName in hiddenApps
        }
    }

    override suspend fun getBlockedApps(): List<BlockedApp> {
        return context.dataStore.data.first().blockedApps
//        return _deviceApps.first().filter { it.isPinned==true }
//        return appsDao.getBlockedApps()
    }

    override suspend fun blockUnblockApp(
        packageName: String,
        blockType: Pair<BlockType, List<UsageTime>>?,
        blockReleaseDate: String?
    ) {
        val app = _deviceApps.first().find { it.packageName == packageName }
        if (app==null) return
        if (blockType==null) return

        val isAppBlocked = context.dataStore.data.first().blockedApps.find { it.packageName==packageName }
        if (isAppBlocked != null){
            context.dataStore.updateData {
                it.copy(
                    blockedApps = it.blockedApps.filter { appN -> appN.packageName != packageName }
                )
            }
        }else{
            context.dataStore.updateData {
                it.copy(
                    blockedApps = it.blockedApps.plus(
                        element = BlockedApp(
                            packageName = packageName,
                            name = app.name,
                            blockType = blockType,
                            domains = app.domains,
                            releaseDate = blockReleaseDate
                        )
                    )
                )
            }
        }
    }

    override suspend fun pinUnpinApp(packageName: String,pin: Boolean) {
        context.dataStore.updateData {
            it.copy(
                pinnedApps = if (pin) {
                    (it.pinnedApps + packageName).distinct()
                } else {
                    it.pinnedApps.filter { appN -> appN != packageName }
                }
            )
        }
    }

    override suspend fun hideUnhideApp(packageName: String, hidden: Boolean) {
        context.dataStore.updateData {
            it.copy(
                hiddenApps = if (hidden) {
                    (it.hiddenApps + packageName).distinct()
                } else {
                    it.hiddenApps.filter { appN -> appN != packageName }
                }
            )
        }
    }

    override fun newAppInstalled(app: App) {
        _deviceApps.update { ls-> ls+app }
    }

    override suspend fun removeUninstalled(packageName: String) {
        val isAppBlocked = context.dataStore.data.first().blockedApps.find { it.packageName==packageName }
        if (isAppBlocked != null){
            context.dataStore.updateData {
                it.copy(
                    blockedApps = it.blockedApps.filter { appN -> appN.packageName != packageName }
                )
            }
        }
        _deviceApps.update { curList->curList.filter { it.packageName != packageName } }
    }
}