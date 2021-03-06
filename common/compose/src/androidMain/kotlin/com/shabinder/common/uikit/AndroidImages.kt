@file:Suppress("FunctionName")

package com.shabinder.common.uikit

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.shabinder.common.database.R
import com.shabinder.common.di.Picture
import com.shabinder.common.di.dispatcherIO
import kotlinx.coroutines.withContext

@Composable
actual fun ImageLoad(link:String, loader:suspend (String) -> Picture, desc: String, modifier:Modifier, placeholder: ImageVector) {
    var pic by remember(link) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(link){
        withContext(dispatcherIO) {
            pic = loader(link).image
        }
    }

    Crossfade(pic){
        if(it == null) Image(placeholder, desc, modifier,contentScale = ContentScale.Crop) else Image(it, desc, modifier,contentScale = ContentScale.Crop)
    }
}

actual fun montserratFont() = FontFamily(
    Font(R.font.montserrat_light, FontWeight.Light),
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
)

actual fun pristineFont() = FontFamily(
    Font(R.font.pristine_script, FontWeight.Bold)
)


@Composable
actual fun DownloadImageTick(){
    Image(
        painterResource(R.drawable.ic_tick),
        "Download Done"
    )
}

@Composable
actual fun DownloadImageError(){
    Image(
        painterResource(R.drawable.ic_error),
        "Error! Cant Download this track"
    )
}

@Composable
actual fun DownloadImageArrow(modifier: Modifier){
    Image(
        painterResource(R.drawable.ic_arrow),
        "Start Download",
        modifier
    )
}

@Composable
actual fun DownloadAllImage() = vectorResource(R.drawable.ic_download_arrow)

@Composable
actual fun ShareImage() = vectorResource(R.drawable.ic_share_open)

@Composable
actual fun PlaceHolderImage() = vectorResource(R.drawable.music)

@Composable
actual fun SpotiFlyerLogo() = vectorResource(R.drawable.ic_spotiflyer_logo)

@Composable
actual fun HeartIcon() = vectorResource(R.drawable.ic_heart)

@Composable
actual fun SpotifyLogo() = vectorResource(R.drawable.ic_spotify_logo)

@Composable
actual fun GaanaLogo() = vectorResource(R.drawable.ic_gaana)

@Composable
actual fun YoutubeLogo() = vectorResource(R.drawable.ic_youtube)

@Composable
actual fun YoutubeMusicLogo() = vectorResource(R.drawable.ic_youtube_music_logo)

@Composable
actual fun GithubLogo() = vectorResource(R.drawable.ic_github)

@Composable
fun vectorResource(@DrawableRes id: Int) =  ImageVector.Companion.vectorResource(id)