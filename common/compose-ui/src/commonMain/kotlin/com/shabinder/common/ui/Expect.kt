package com.shabinder.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.shabinder.common.Picture

@Composable
expect fun ImageLoad(
    pic: Picture?,
    modifier: Modifier = Modifier
)

expect fun showPopUpMessage(text: String)