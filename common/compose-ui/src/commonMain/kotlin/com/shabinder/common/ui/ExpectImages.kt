@file:Suppress("FunctionName")
package com.shabinder.common.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.withContext

@Composable
fun ImageLoad(link:String,loader:suspend (String) ->ImageBitmap?, desc: String = "Album Art", modifier:Modifier = Modifier, placeholder:ImageVector = PlaceHolderImage()) {
    var pic by remember(link) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(link){
        withContext(dispatcherIO) {
            pic = loader(link)
        }
    }
    Crossfade(pic){
        if(it == null) Image(placeholder, desc, modifier,contentScale = ContentScale.Crop) else Image(it, desc, modifier,contentScale = ContentScale.Crop)
    }
}

@Composable
expect fun DownloadImageTick()

@Composable
expect fun DownloadAllImage():ImageVector

@Composable
expect fun ShareImage():ImageVector

@Composable
expect fun PlaceHolderImage():ImageVector

@Composable
expect fun SpotiFlyerLogo():ImageVector

@Composable
expect fun SpotifyLogo():ImageVector

@Composable
expect fun YoutubeLogo():ImageVector

@Composable
expect fun GaanaLogo():ImageVector

@Composable
expect fun YoutubeMusicLogo():ImageVector

@Composable
expect fun GithubLogo():ImageVector

@Composable
expect fun HeartIcon():ImageVector

@Composable
expect fun DownloadImageError()

@Composable
expect fun DownloadImageArrow(modifier: Modifier)