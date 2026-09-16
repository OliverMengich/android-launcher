package com.planara.android_launcher.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.util.Patterns
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.planara.android_launcher.BlockedAppActivity
import com.planara.android_launcher.dataStore
import com.planara.android_launcher.domain.models.BlockType
import com.planara.android_launcher.domain.models.BlockedApp
import com.planara.android_launcher.domain.repository.AppsRepository
import com.planara.android_launcher.utils.formatIsoTimeToFriendly
import com.planara.android_launcher.utils.isDatePassed
import com.planara.android_launcher.utils.isNowBetween
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.URL

class MyAccessibilityService : AccessibilityService(), KoinComponent {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val appsRepository: AppsRepository by inject()

    private val browserAddressBarIds = mapOf(
        "com.android.chrome" to listOf(
            "com.android.chrome:id/url_bar",
            "com.android.chrome:id/search_box_text"
        ),
        "org.mozilla.firefox" to listOf(
            "org.mozilla.firefox:id/toolbar_url",
            "org.mozilla.firefox:id/url_bar_title",
            "org.mozilla.firefox:id/mozac_browser_toolbar_url_view"
        ),
        "com.brave.browser" to listOf(
            "com.brave.browser:id/url_bar",
            "com.brave.browser:id/search_box_text"
        ),
        "com.microsoft.emmx" to listOf(
            "com.microsoft.emmx:id/url_bar"
        ),
        "com.opera.browser" to listOf(
            "com.opera.browser:id/url_field"
        ),
        "com.sec.android.app.sbrowser" to listOf(
            "com.sec.android.app.sbrowser:id/location_bar_edit_text"
        )
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        ) {
            val packageName = event.packageName?.toString() ?: return

            if (isBrowser(packageName)) {
                // Fetch the root active node or fallback to event source
                val rootNode = rootInActiveWindow ?: event.source ?: return
                val currentUrl = getCurrentUrl(rootNode, packageName)

                if (currentUrl.isBlank()) return

                serviceScope.launch {
                    val blockedApps = dataStore.data.map { it.blockedApps }.first()
                    Log.d("allText", "Active URL: $currentUrl. blockedApps=$blockedApps")

                    val (isBlocked, ap) = isDomainSimilar(currentUrl = currentUrl, blockedList = blockedApps)
                    Log.d("allText", "isBlocked=$isBlocked Blocked package=${ap?.name}")

                    if (isBlocked && ap != null) {
                        when (ap.blockType.first) {
                            BlockType.NORMAL -> {
                                if (isDatePassed(ap.releaseDate)) {
                                    dataStore.updateData {
                                        it.copy(
                                            blockedApps = it.blockedApps.filter { blockedApp -> blockedApp.packageName != ap.packageName }
                                        )
                                    }
                                    return@launch
                                } else {
                                    Intent(this@MyAccessibilityService, BlockedAppActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        putExtra("app_name", ap.name)
                                        putExtra("message", "You block ${ap.name} until ${formatIsoTimeToFriendly(input = ap.releaseDate)}. Digital detox is working, keep moving")
                                    }.also { startActivity(it) }
                                }
                            }
                            BlockType.USE_BETWEEN -> {
                                if (isDatePassed(ap.releaseDate)) {
                                    dataStore.updateData {
                                        it.copy(
                                            blockedApps = it.blockedApps.filter { blockedApp -> blockedApp.packageName != ap.packageName }
                                        )
                                    }
                                    return@launch
                                }
                                ap.blockType.second.forEach { (startTime, endTime) ->
                                    if (isNowBetween(startTime, endTime)) {
                                        return@launch
                                    } else {
                                        Intent(this@MyAccessibilityService, BlockedAppActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            putExtra("app_name", ap.name)
                                            val msg = ap.blockType.second.joinToString { (startTime, endTime) -> "$startTime - $endTime" }
                                            putExtra("message", "You block ${ap.name} and you can only use it at $msg until ${formatIsoTimeToFriendly(input = ap.releaseDate)}. Digital detox is working, keep moving")
                                        }.also { startActivity(it) }
                                    }
                                }
                            }
                            BlockType.NOT_USE -> {
                                if (isDatePassed(ap.releaseDate)) {
                                    dataStore.updateData {
                                        it.copy(
                                            blockedApps = it.blockedApps.filter { blockedApp -> blockedApp.packageName != ap.packageName }
                                        )
                                    }
                                    return@launch
                                }
                                ap.blockType.second.forEach { (startTime, endTime) ->
                                    if (isNowBetween(startTime, endTime)) {
                                        Intent(this@MyAccessibilityService, BlockedAppActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            putExtra("app_name", ap.name)
                                            val msg = ap.blockType.second.joinToString { (startTime, endTime) -> "$startTime - $endTime" }
                                            putExtra("message", "You block ${ap.name} and you cannot use it at $msg until ${formatIsoTimeToFriendly(input = ap.releaseDate)}. Digital detox is working, keep moving.")
                                        }.also { startActivity(it) }
                                    } else {
                                        return@launch
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun isBrowser(packageName: String): Boolean {
        return packageName in listOf(
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.brave.browser",
            "com.microsoft.emmx",
            "com.opera.browser",
            "com.sec.android.app.sbrowser"
        )
    }

    private fun getCurrentUrl(rootNode: AccessibilityNodeInfo?, packageName: String): String {
        if (rootNode == null) return ""

        // 1. Target known browser omnibox/URL bar View IDs
        val targetIds = browserAddressBarIds[packageName] ?: emptyList()
        for (viewId in targetIds) {
            val matchingNodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
            for (node in matchingNodes) {
                val text = node.text?.toString() ?: ""
                if (isValidAddressBarUrl(text)) {
                    return extractBaseURL(text)
                }
            }
        }

        // 2. Controlled search targeting address bar EditTexts/inputs
        return findUrlInAddressBarFallback(rootNode)
    }

    private fun findUrlInAddressBarFallback(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""

        val className = node.className?.toString() ?: ""
        val isInputField = className.contains("EditText", ignoreCase = true) || node.isEditable

        if (isInputField) {
            val text = node.text?.toString() ?: ""
            if (isValidAddressBarUrl(text)) {
                return extractBaseURL(text)
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findUrlInAddressBarFallback(child)
            if (result.isNotEmpty()) return result
        }

        return ""
    }

    private fun isValidAddressBarUrl(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || trimmed.contains(" ") || !trimmed.contains(".")) {
            return false
        }
        return trimmed.startsWith("http://") ||
                trimmed.startsWith("https://") ||
                Patterns.WEB_URL.matcher(trimmed).matches()
    }

    private fun extractBaseURL(url: String): String {
        var cleanUrl = url.trim()
        cleanUrl = cleanUrl.removePrefix("https://").removePrefix("http://")
        cleanUrl = cleanUrl.removePrefix("www.")
        val slashIndex = cleanUrl.indexOf("/")
        if (slashIndex != -1) {
            cleanUrl = cleanUrl.take(slashIndex)
        }
        return cleanUrl
    }

    private fun isDomainSimilar(currentUrl: String, blockedList: List<BlockedApp>): Pair<Boolean, BlockedApp?> {
        for (blocked in blockedList) {
            val isMatch = currentUrl in blocked.domains
            if (isMatch) return Pair(true, blocked)
        }
        return Pair(false, null)
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            packageNames = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        serviceScope.cancel()
    }
}