/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:Suppress("FunctionName")
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorXmlResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import com.shabinder.common.di.Picture
import com.shabinder.common.di.dispatcherIO
import com.shabinder.common.models.methods
import kotlinx.coroutines.withContext

@Composable
actual fun ImageLoad(
    link: String,
    loader: suspend (String) -> Picture,
    desc: String,
    modifier: Modifier,
    // placeholder: ImageVector
) {
    var pic by remember(link) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(link) {
        withContext(dispatcherIO) {
            pic = loader(link).image
        }
    }

    Crossfade(pic) {
        if (it == null) Image(PlaceHolderImage(), desc, modifier, contentScale = ContentScale.Crop) else Image(it, desc, modifier, contentScale = ContentScale.Crop)
    }
}

@Composable
actual fun DownloadImageTick() {
    Image(
        vectorXmlResource("drawable/ic_tick.xml"),
        "Downloaded"
    )
}

@Composable
actual fun DonationDialog(
    isVisible:Boolean,
    onDismiss:()->Unit
){

}

actual fun montserratFont() = FontFamily(
    Font("font/montserrat_light.ttf", FontWeight.Light),
    Font("font/montserrat_regular.ttf", FontWeight.Normal),
    Font("font/montserrat_medium.ttf", FontWeight.Medium),
    Font("font/montserrat_semibold.ttf", FontWeight.SemiBold),
)

actual fun pristineFont() = FontFamily(
    Font("font/pristine_script.ttf", FontWeight.Bold)
)

@Composable
actual fun DownloadImageError() {
    Image(
        vectorXmlResource("drawable/ic_error.xml"),
        "Can't Download"
    )
}

@Composable
actual fun DownloadImageArrow(modifier: Modifier) {
    Image(
        vectorXmlResource("drawable/ic_arrow.xml"),
        "Download",
        modifier
    )
}

@Composable
actual fun DownloadAllImage() = rememberVectorPainter(vectorXmlResource("drawable/ic_download_arrow.xml")) as Painter

@Composable
actual fun ShareImage() = rememberVectorPainter(vectorXmlResource("drawable/ic_share_open.xml"))  as Painter

@Composable
actual fun PlaceHolderImage() = rememberVectorPainter(vectorXmlResource("drawable/music.xml"))
        as Painter
@Composable
actual fun SpotiFlyerLogo() =
    rememberVectorPainter(vectorXmlResource("drawable/ic_spotiflyer_logo.xml")) as Painter

@Composable
actual fun HeartIcon() = rememberVectorPainter(vectorXmlResource("drawable/ic_heart.xml")) as Painter

@Composable
actual fun SpotifyLogo() = rememberVectorPainter(vectorXmlResource("drawable/ic_spotify_logo.xml"))  as Painter

@Composable
actual fun YoutubeLogo() = rememberVectorPainter(vectorXmlResource("drawable/ic_youtube.xml"))  as Painter

@Composable
actual fun GaanaLogo() = rememberVectorPainter(vectorXmlResource("drawable/ic_gaana.xml")) as Painter

@Composable
actual fun YoutubeMusicLogo() = rememberVectorPainter(vectorXmlResource("drawable/ic_youtube_music_logo.xml")) as Painter

@Composable
actual fun GithubLogo() = rememberVectorPainter(vectorXmlResource("drawable/ic_github.xml")) as Painter

@Composable
actual fun PaypalLogo() = rememberVectorPainter(vectorXmlResource("drawable/ic_paypal_logo.xml")) as Painter

@Composable
actual fun RazorPay() = rememberVectorPainter(vectorXmlResource("drawable/ic_indian_rupee.xml")) as Painter
