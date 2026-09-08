package com.planara.android_launcher.domain.models

data class DeviceCalendar(
    val id: Long,
    val name: String,
    val accountName: String,
    /** e.g. "com.google", "com.android.exchange", "LOCAL". */
    val accountType: String = "",
    /** ARGB color of the calendar, if the provider exposes one. */
    val color: Int = 0,
) {
    /** How the calendar is shown in pickers, e.g. "Work · you@gmail.com". */
    val displayLabel: String
        get() = if (accountName.isBlank() || accountName == name) name else "$name · $accountName"
}
