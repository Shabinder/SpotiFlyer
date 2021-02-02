package com.shabinder.common.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.core.net.toUri
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
actual fun ImageLoad(
    url:String,
    loadingResource:ImageBitmap?,
    errorResource:ImageBitmap?,
    modifier: Modifier
){
    val imgUri = url.toUri().buildUpon().scheme("https").build()
    CoilImage(
        data = imgUri,
        contentScale = ContentScale.Crop,
        loading = { loadingResource?.let { Image(it,"loading image") } },
        error = { errorResource?.let { it1 -> Image(it1,"Error Image") } },
        modifier = modifier
    )
}
