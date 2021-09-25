package com.shabinder.common.uikit

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.shabinder.common.core_components.picture.Picture
import com.shabinder.common.models.dispatcherIO
import kotlinx.coroutines.withContext

@Composable
actual fun ImageLoad(
    link: String,
    loader: suspend () -> Picture,
    desc: String,
    modifier: Modifier
    // placeholder: ImageVector
) {
    var pic by remember(link) {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(link) {
        withContext(dispatcherIO) {
            pic = loader().image
        }
    }

    Crossfade(pic) {
        if (it == null) {
            Image(PlaceHolderImage(), desc, modifier, contentScale = ContentScale.Crop)
        } else Image(it, desc, modifier, contentScale = ContentScale.Crop)
    }
}
