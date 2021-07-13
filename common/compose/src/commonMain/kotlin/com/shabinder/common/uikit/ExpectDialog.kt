package com.shabinder.common.uikit

import androidx.compose.runtime.Composable

@Composable
expect fun Dialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
)
