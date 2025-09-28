package com.example.android_launcher

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
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
            Intent.ACTION_PACKAGE_ADDED->{
                Log.d("new_app","new app installed")
                Log.d("APP_PACKAGE_CHANGED","Package ADDED: ${intent.data?.encodedSchemeSpecificPart}")
                val packageName = intent.data?.encodedSchemeSpecificPart ?: return
                val replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                Log.d("APP_PACKAGE_CHANGED","Package ADDED: $packageName. REPLACING=$replacing")
                if (replacing) return // ignore if it's an app update
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val appInfo = pm?.getApplicationInfo(packageName,0) ?: return@launch
                        val appLabel = pm.getApplicationLabel(appInfo).toString()

                        val appDomains = getDomainsFromApp(appLabel)
                        val category = getCategoryName(appInfo.category)
                        appsRepository.newAppInstalled(
                            app = App(packageName = packageName, category = category, domains = appDomains, name = appLabel)
                        )
                        appsRepository.refetchAppsFlow.tryEmit(true)
                    }catch (e: Exception){
                        Log.e("AppInstallEvent","App not found ${e.message}")
                    }finally {
                        Log.d("AppInstallEvent","Finished install")
                        pendingResult.finish()
                    }
                }
            }
            Intent.ACTION_PACKAGE_REMOVED->{
                val packageName = intent.data?.encodedSchemeSpecificPart ?: return
                Log.d("APP_PACKAGE_CHANGED","Package removed: $packageName")
                val replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                if (replacing) return // ignore if it's an app update
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        appsRepository.removeUninstalled(packageName)
                        appsRepository.refetchAppsFlow.tryEmit(true)
                    }catch (e: Exception){
                        Log.e("AppInstallEvent","App not found ${e.message}")
                    }finally {
                        Log.d("AppInstallEvent","Finished uninstall")
                        pendingResult.finish()
                    }
                }
            }
        }
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
    fun getDomainsFromApp(appName: String): List<String> {
        val normalized = appName
            .trim()
            .lowercase()
            .replace(" ", "")
            .replace("_", "")
            .replace("-", "")

        val domains = mutableSetOf<String>()

        // Base domain options
        domains.add("$normalized.com")
        domains.add("www.$normalized.com")
        domains.add("m.$normalized.com")

        // Add variations for some special apps
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

}