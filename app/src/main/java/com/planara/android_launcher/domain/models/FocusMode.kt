package com.planara.android_launcher.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Scheduling modes for Focus Mode, mirrored after the app blocking modes.
 *
 * - [NORMAL]:               focus is on all day (every day) until [FocusMode.endDate].
 * - [FOCUS_BETWEEN]:        focus is only on inside the selected windows (i.e. "focus at specific times").
 * - [NO_FOCUS_BETWEEN]:     focus is on all day except inside the selected windows (i.e. "don't focus at specific times").
 */
enum class FocusType {
    NORMAL,
    FOCUS_BETWEEN,
    NO_FOCUS_BETWEEN
}

/**
 * A single focus session. Several sessions can be enabled at the same time, e.g. a long
 * "focus until the end date" session plus a short "today 10am - 1pm" session.
 */
@Serializable
data class FocusMode(
    /** Stable identifier so sessions can be edited/removed independently. */
    val id: String = UUID.randomUUID().toString(),
    /** When the session stops being enforced. */
    val endDate: String = "",
    /** When the session starts being enforced. Blank means "from now". */
    val startDate: String = "",
    /** Effective scheduling type. Missing in older saved data, so it defaults to a legacy-compatible value. */
    val focusType: FocusType = FocusType.NORMAL,
    /** Windows that focus (FOCUS_BETWEEN) or don't focus (NO_FOCUS_BETWEEN) depending on [focusType]. */
    val usageTimes: List<UsageTime> = emptyList(),
    // Legacy single range kept so previously saved focus sessions keep working.
    val startTime: String = "",
    val endTime: String = ""
)
