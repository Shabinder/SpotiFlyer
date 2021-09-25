package com.shabinder.common.uikit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource

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
            title = "SpotiFlyer",
            icon = BitmapPainter(useResource("drawable/spotiflyer.png", ::loadImageBitmap))
        ) {
            content()
        }
    }
}
