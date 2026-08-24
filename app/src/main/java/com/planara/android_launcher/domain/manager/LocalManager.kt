package com.planara.android_launcher.domain.manager

import androidx.compose.runtime.Immutable
import com.planara.android_launcher.domain.models.BlockedApp
import com.planara.android_launcher.domain.models.DisplaySettings
import com.planara.android_launcher.domain.models.FocusMode
import com.planara.android_launcher.domain.models.FocusType
import com.planara.android_launcher.domain.models.UsageTime
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Immutable
@Serializable
data class LocalManager(
    val activeTab: Int = 1,
    val phoneApp: String = "",
    val clockApp: String = "",
    val cameraApp: String = "",
    val displaySettings: DisplaySettings=DisplaySettings(),
    val isLoggedIn: Boolean = false,
    val focusMode: FocusMode? = null,
    val focusModes: List<FocusMode> = emptyList(),
    val blockedApps: List<BlockedApp> = emptyList(),
    val blockedWebsites: List<String> = emptyList(),
    val selectedDeviceCalendar: Long = 0L,
    val hiddenApps: List<String> = emptyList(),
    val pinnedApps: List<String> = emptyList()
) {
    /**
     * All focus sessions, including the single legacy `focusMode` slot so previously saved
     * sessions keep working. Legacy sessions (with only `startTime`/`endTime`) are normalized
     * into the time-window format.
     */
    fun allFocusModes(): List<FocusMode> {
        val legacy = focusMode?.let { normalizeLegacy(it) }
        return (listOfNotNull(legacy) + focusModes).distinctBy { it.id }
    }

    /** Sessions that have not expired yet, i.e. the ones that are active or scheduled. */
    fun activeFocusModes(): List<FocusMode> = allFocusModes().filter { !it.isExpired() }

    private fun normalizeLegacy(mode: FocusMode): FocusMode {
        if (mode.usageTimes.isNotEmpty() || mode.startTime.isEmpty() || mode.endTime.isEmpty()) return mode
        return mode.copy(
            focusType = FocusType.FOCUS_BETWEEN,
            usageTimes = listOf(UsageTime(startTime = mode.startTime, endTime = mode.endTime)),
            startTime = "",
            endTime = ""
        )
    }
}

private fun FocusMode.isExpired(): Boolean {
    if (endDate.isBlank()) return false
    return runCatching {
        LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .isBefore(LocalDateTime.now())
    }.getOrDefault(false)
}
