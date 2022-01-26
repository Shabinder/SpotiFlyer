package com.shabinder.common.uikit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogState

@OptIn(ExperimentalAnimationApi::class)
@Composable
actual fun Dialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(isVisible) {
        androidx.compose.ui.window.Dialog(
            onDismiss,
            state = DialogState(width = 350.dp, height = 340.dp),
            title = "SpotiFlyer",
            icon = BitmapPainter(useResource("drawable/spotiflyer.png", ::loadImageBitmap))
        ) {
            content()
        }
    }
}
