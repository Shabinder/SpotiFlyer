package com.shabinder.common.uikit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shabinder.common.core_components.picture.Picture

@Composable
expect fun ImageLoad(
    link: String,
    loader: suspend () -> Picture,
    desc: String,
    modifier: Modifier
    // placeholder:Painter = PlaceHolderImage()
)
