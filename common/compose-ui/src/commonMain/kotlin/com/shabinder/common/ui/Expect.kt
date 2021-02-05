package com.shabinder.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp

@Composable
expect fun ImageLoad(
    url:String,
    loadingResource: ImageBitmap? = null,
    errorResource: ImageBitmap? = null,
    modifier: Modifier = Modifier
)

expect fun showPopUpMessage(text: String)