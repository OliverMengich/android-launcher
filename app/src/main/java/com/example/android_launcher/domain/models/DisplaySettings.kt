package com.example.android_launcher.domain.models

import kotlinx.serialization.Serializable


@Serializable
data class DisplaySettings(
    val autoOpenKeyboard: Boolean = false,
    val theme: String="SYSTEM",
    val currentFont: AppFonts = AppFonts.Default,
    val timeFormat: String = "HH:mm",
    val dateFormat: String = "custom1",
)

enum class AppFonts{
    Default,
    Inter,
    SpaceGrotesk,
    Montserrat,
    Sarina,
    CrosiantMono,
    DancingScript
}
