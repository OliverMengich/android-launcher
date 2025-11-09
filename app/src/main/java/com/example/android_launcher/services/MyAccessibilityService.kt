package com.example.android_launcher.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.example.android_launcher.BlockedAppActivity
import com.example.android_launcher.domain.repository.AppsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.URL
import androidx.core.net.toUri
import com.example.android_launcher.domain.models.App
import com.example.android_launcher.utils.isDatePassed

class MyAccessibilityService: AccessibilityService(), KoinComponent {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO+serviceJob)
    private val appsRepository: AppsRepository by inject()
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        serviceScope.launch {
            val nodeInfo = event?.source ?: return@launch
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                val packageName = event.packageName?.toString() ?: return@launch
                if (isBrowser(packageName)) {
                    val blockedApps = appsRepository.getBlockedApps()
                    val allText = getCurrentUrl(node=nodeInfo)
                    Log.d("allText","all text is =$allText. blockedApps=$blockedApps")
                    val (isBlocked,ap ) = isDomainSimilar(currentUrl = allText, blockedList = blockedApps)
                    Log.d("allText","isBlocked=$isBlocked Blocked package=${ap?.name}")
                    if (isBlocked) {
                        if (ap?.blockReleaseDate != null && isDatePassed(ap.blockReleaseDate)) {
                            Log.d("passed", "time is passed")
                            appsRepository.blockUnblockApp(ap.packageName, 0, null)
                        }else{
                            showBlockedActivity(appName = ap?.name,ap?.blockReleaseDate)
                        }
                    }
                }
            }
        }
    }
    private fun showBlockedActivity(appName: String?, releaseDate: String?){
        val intent = Intent(this, BlockedAppActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("app_name", appName)
            putExtra("release_date",releaseDate)
        }
        startActivity(intent)
    }

    private fun isBrowser(packageName: String): Boolean {
        return packageName in listOf(
            "com.android.chrome",
            "org.mozilla.firefox.com",
            "com.brave.browser",
            "com.microsoft.emmx" // Edge
        )
    }

    private fun getCurrentUrl(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""
        val text = node.text?.toString() ?: ""
        if (text.startsWith("http") || text.contains(".com") || text.contains(".co")) {
            return extractBaseURL(text)
//            return text
        }
        for (i in 0 until node.childCount) {
            val childText = getCurrentUrl(node.getChild(i))
//            if (childText.isNotEmpty()) return childText
            if (childText.isNotEmpty()) return  extractBaseURL(childText)
        }
        return ""
    }

    private fun extractBaseURL(url: String): String{
        var cleanUrl = url.trim()
        cleanUrl = cleanUrl.removePrefix("https://").removePrefix("http://")
        cleanUrl = cleanUrl.removePrefix("www")
        val slashIndex = cleanUrl.indexOf("/")
        if (slashIndex != -1) {
            cleanUrl = cleanUrl.take(n=slashIndex)
        }
        return cleanUrl
    }
    override fun onInterrupt() {
        // Required override
    }
    private fun isDomainSimilar(currentUrl: String, blockedList: List<App>): Pair<Boolean, App?> {
//        val currentHost = extractHost(currentUrl) ?: return Pair(false, "")
        for (blocked in blockedList) {
            val isMatch = currentUrl in blocked.domains
//            val isMatch = currentUrl.contains(blocked,ignoreCase = true)
//            val normalizedUrl = normalizeUrlHost(currentUrl)
//            val canonicalPkg = canonicalizePackageName(blocked)
//            Log.d("allText","normalizedUrl=$normalizedUrl, canonicalPkg=$canonicalPkg")
////            val blockedHost = extractHost(blocked) ?: continue
////            val isMatch = currentHost == blockedHost || currentHost.endsWith(".$blockedHost")
//            val isMatch = normalizedUrl == canonicalPkg
            if (isMatch) return Pair(true, blocked)
        }
        return Pair(false, null)
    }

    fun canonicalizePackageName(packageName: String): String {
        return packageName
            .lowercase()
            .split(".")
            .filter { it.isNotBlank() && it !in listOf("com", "android", "google", "org", "net", "app") }
            .joinToString(".")
    }

    private fun extractHost(url: String): String? {
        return try {
            val cleanUrl = if (url.startsWith("http")) url else "https://$url"
            URL(cleanUrl).host.removePrefix("www.")
        } catch (e: Exception) {
            null
        }
    }
    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            packageNames = null // Listen to all apps
        }
    }

}