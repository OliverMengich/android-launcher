package com.planara.android_launcher.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class DialogState {
    // History stack to support back-navigation through steps
    private val steps = mutableStateListOf<@Composable (DialogState) -> Unit>()

    val isVisible: Boolean get() = steps.isNotEmpty()
    val currentStepIndex: Int get() = steps.lastIndex
    val currentContent: (@Composable (DialogState) -> Unit)? get() = steps.lastOrNull()

    // Show a single-step dialog
    fun show(content: @Composable (DialogState) -> Unit) {
        steps.clear()
        steps.add(content)
    }

    // Push the next step in a multi-step flow
    fun pushStep(content: @Composable (DialogState) -> Unit) {
        steps.add(content)
    }

    // Pop the current step; dismisses if on the first step
    fun popStep(): Boolean {
        return if (steps.size > 1) {
            steps.removeAt(steps.lastIndex)
            true
        } else {
            dismiss()
            false
        }
    }

    fun dismiss() {
        steps.clear()
    }
}

@Composable
fun rememberDialogState(): DialogState = remember { DialogState() }
@Composable
fun SharedDialog(
    state: DialogState,
    modifier: Modifier = Modifier,
    dialogProperties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = false,
    )
) {
    if (!state.isVisible) return

    val content = state.currentContent ?: return
    val shape = RoundedCornerShape(20.dp)

    Dialog(
        onDismissRequest = { state.dismiss() },
        properties = dialogProperties
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.90f)
                .background(MaterialTheme.colorScheme.background,shape = RoundedCornerShape(20.dp))
                .wrapContentHeight()
                .padding(1.dp)
        ) {
            AnimatedContent(
                targetState = state.currentStepIndex to content,
                modifier = modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = shape
                    ),
                transitionSpec = {
                    if (targetState.first > initialState.first) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "StepAnimation"
            ) { (_, stepComposable) ->
                stepComposable(state)
            }
        }
    }
}