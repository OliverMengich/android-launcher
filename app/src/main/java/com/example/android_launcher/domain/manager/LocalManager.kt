package com.example.android_launcher.domain.manager

import com.example.android_launcher.domain.models.DisplaySettings
import com.example.android_launcher.domain.models.FocusMode
import kotlinx.serialization.Serializable

@Serializable
data class LocalManager(
    val phoneApp: String = "",
    val clockApp: String = "",
    val cameraApp: String = "",
    val displaySettings: DisplaySettings=DisplaySettings(),
    val isLoggedIn: Boolean = false,
    val focusMode: FocusMode = FocusMode(),
)