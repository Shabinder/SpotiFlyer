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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import com.shabinder.common.database.R
import com.shabinder.common.translations.Strings
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
internal actual fun <T> imageVectorResource(id: T): ImageVector =
    ImageVector.Companion.vectorResource(id as Int)

@Composable
actual fun DownloadImageTick() {
    Image(
        getCachedPainter(R.drawable.ic_tick),
        Strings.downloadDone()
    )
}

@Composable
actual fun DownloadImageError(modifier: Modifier) {
    Image(
        getCachedPainter(R.drawable.ic_error),
        Strings.downloadError(),
        modifier = modifier
    )
}

@Composable
actual fun DownloadImageArrow(modifier: Modifier) {
    Image(
        getCachedPainter(R.drawable.ic_arrow),
        Strings.downloadStart(),
        modifier
    )
}

@Composable
actual fun DownloadAllImage() = getCachedPainter(R.drawable.ic_download_arrow)

@Composable
actual fun ShareImage() = getCachedPainter(R.drawable.ic_share_open)

@Composable
actual fun PlaceHolderImage() = getCachedPainter(R.drawable.ic_song_placeholder)

@Composable
actual fun SpotiFlyerLogo() = getCachedPainter(R.drawable.ic_spotiflyer_logo)

@Composable
actual fun HeartIcon() = painterResource(R.drawable.ic_heart)

@Composable
actual fun SpotifyLogo() = getCachedPainter(R.drawable.ic_spotify_logo)

@Composable
actual fun SaavnLogo() = getCachedPainter(R.drawable.ic_jio_saavn_logo)

@Composable
actual fun SoundCloudLogo() = getCachedPainter(R.drawable.ic_soundcloud)

@Composable
actual fun GaanaLogo() = getCachedPainter(R.drawable.ic_gaana)

@Composable
actual fun YoutubeLogo() = getCachedPainter(R.drawable.ic_youtube)

@Composable
actual fun YoutubeMusicLogo() = getCachedPainter(R.drawable.ic_youtube_music_logo)

@Composable
actual fun GithubLogo() = getCachedPainter(R.drawable.ic_github)

@Composable
actual fun PaypalLogo() = painterResource(R.drawable.ic_paypal_logo)

@Composable
actual fun OpenCollectiveLogo() = painterResource(R.drawable.ic_opencollective_icon)

@Composable
actual fun RazorPay() = painterResource(R.drawable.ic_indian_rupee)

@Composable
actual fun Toast(
    flow: MutableStateFlow<String>,
    duration: ToastDuration
) {
    // We Have Android's Implementation of Toast so its just Empty
}