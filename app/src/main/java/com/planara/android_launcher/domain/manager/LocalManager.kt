package com.planara.android_launcher.domain.manager

import com.planara.android_launcher.domain.models.BlockedApp
import com.planara.android_launcher.domain.models.DisplaySettings
import com.planara.android_launcher.domain.models.FocusMode
import kotlinx.serialization.Serializable

@Serializable
data class LocalManager(
    val phoneApp: String = "",
    val clockApp: String = "",
    val cameraApp: String = "",
    val displaySettings: DisplaySettings=DisplaySettings(),
    val isLoggedIn: Boolean = false,
    val focusMode: FocusMode = FocusMode(),
    val blockedApps: List<BlockedApp> = emptyList(),
    val blockedWebsites: List<String> = emptyList(),
    val selectedDeviceCalendar: Long = 0L,
    val hiddenApps: List<String> = emptyList(),
    val pinnedApps: List<String> = emptyList()
)