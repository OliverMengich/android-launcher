package com.example.android_launcher.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class FocusMode(
    val isActive: Boolean = false,
    val endTime: String = ""
)
