package com.planara.android_launcher.presentation.screens.home.home.focus

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planara.android_launcher.dataStore
import com.planara.android_launcher.domain.manager.LocalManager
import com.planara.android_launcher.domain.models.FocusMode
import com.planara.android_launcher.utils.isActive
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Focus sessions can only be ended by reaching their end date. There is deliberately no
 * "turn off" / "remove" API here so nothing in the UI can cancel a running session.
 */
class FocusModeViewModel(private val context: Context) : ViewModel() {

    val localManagerData = context.dataStore.data.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = LocalManager()
    )

    /**
     * Adds a new focus session next to any existing ones, or replaces the session with the same
     * [FocusMode.id] when editing. Sessions are independent, so several can run at the same time.
     *
     * A session that is currently in effect cannot be edited or replaced, so focus can't be
     * shortened from the device once it is running.
     *
     * @return null on success, or a user-facing error message when the save was rejected.
     */
    suspend fun saveFocusMode(focusMode: FocusMode): String? {
        val prefs = context.dataStore.data.first()
        val existing = prefs.allFocusModes()
        if (existing.any { it.id == focusMode.id && it.isActive() }) {
            return "This focus session is active and can't be changed until it ends"
        }
        context.dataStore.updateData { current ->
            val updated = current.allFocusModes().filter { it.id != focusMode.id } + focusMode
            // Fold any legacy single-slot session into the list so it is only stored once,
            // but keep the legacy slot when it is the one being edited so its id stays valid.
            if (current.focusMode != null && current.focusMode.id == focusMode.id) {
                current.copy(focusMode = focusMode, focusModes = current.focusModes.filter { it.id != focusMode.id })
            } else {
                current.copy(focusMode = null, focusModes = updated)
            }
        }
        return null
    }
}
