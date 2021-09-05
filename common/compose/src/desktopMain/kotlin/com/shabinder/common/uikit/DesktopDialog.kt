package com.shabinder.common.uikit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable

@OptIn(ExperimentalAnimationApi::class)
@Composable
actual fun Dialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(isVisible) {
        androidx.compose.ui.window.Dialog(onDismiss) {
            content()
        }
    }
}
