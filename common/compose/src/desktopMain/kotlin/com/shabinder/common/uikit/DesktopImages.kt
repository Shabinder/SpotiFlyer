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

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.res.useResource
import org.xml.sax.InputSource

@Composable
internal actual fun <T> imageVectorResource(id: T): ImageVector {
    val density = LocalDensity.current
    return useResource(id as String) {
        loadXmlImageVector(InputSource(it), density)
    }
}

@Composable
actual fun DownloadImageTick() {
    Image(
        getCachedPainter("drawable/ic_tick.xml"),
        "Downloaded"
    )
}

@Composable
actual fun DownloadImageError(modifier: Modifier) {
    Image(
        getCachedPainter("drawable/ic_error.xml"),
        "Can't Download",
        modifier = modifier
    )
}

@Composable
actual fun DownloadImageArrow(modifier: Modifier) {
    Image(
        getCachedPainter("drawable/ic_arrow.xml"),
        "Download",
        modifier
    )
}

@Composable
actual fun DownloadAllImage() = getCachedPainter("drawable/ic_download_arrow.xml")


@Composable
actual fun ShareImage() = getCachedPainter("drawable/ic_share_open.xml")

@Composable
actual fun PlaceHolderImage() = getCachedPainter("drawable/music.xml")

@Composable
actual fun SpotiFlyerLogo() = getCachedPainter("drawable/ic_spotiflyer_logo.xml")

@Composable
actual fun HeartIcon() =
    getCachedPainter("drawable/ic_heart.xml")

@Composable
actual fun SpotifyLogo() =
    getCachedPainter("drawable/ic_spotify_logo.xml")

@Composable
actual fun SaavnLogo() =
    getCachedPainter("drawable/ic_jio_saavn_logo.xml")

@Composable
actual fun SoundCloudLogo() =
    getCachedPainter("drawable/ic_soundcloud.xml")

@Composable
actual fun YoutubeLogo() =
    getCachedPainter("drawable/ic_youtube.xml")

@Composable
actual fun GaanaLogo() =
    getCachedPainter("drawable/ic_gaana.xml")

@Composable
actual fun YoutubeMusicLogo() =
    getCachedPainter("drawable/ic_youtube_music_logo.xml")

@Composable
actual fun GithubLogo() =
    getCachedPainter("drawable/ic_github.xml")

@Composable
actual fun PaypalLogo() =
    getCachedPainter("drawable/ic_paypal_logo.xml")

@Composable
actual fun OpenCollectiveLogo() =
    getCachedPainter("drawable/ic_opencollective_icon.xml")

@Composable
actual fun RazorPay() =
    getCachedPainter("drawable/ic_indian_rupee.xml")
