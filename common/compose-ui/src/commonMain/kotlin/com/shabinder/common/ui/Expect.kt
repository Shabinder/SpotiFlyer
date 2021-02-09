package com.shabinder.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shabinder.common.di.Picture

@Composable
expect fun ImageLoad(
    pic: Picture?,
    modifier: Modifier = Modifier
)

expect fun showPopUpMessage(text: String)