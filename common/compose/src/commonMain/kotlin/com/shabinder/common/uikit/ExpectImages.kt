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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.shabinder.common.caching.Cache

private val ImageCache = Cache.Builder.newBuilder()
    .maximumCacheSize(15).build<Any, ImageVector>()

@Composable
internal expect fun <T> imageVectorResource(id: T): ImageVector

@Composable
fun <K : Any> getCachedPainter(key: K): Painter {
    return rememberVectorPainter(
        ImageCache.get(key) ?: imageVectorResource(key).also { ImageCache.put(key, it) })
}

@Composable
expect fun DownloadImageTick()

@Composable
expect fun DownloadAllImage(): Painter

@Composable
expect fun ShareImage(): Painter

@Composable
expect fun PlaceHolderImage(): Painter

@Composable
expect fun SpotiFlyerLogo(): Painter

@Composable
expect fun SpotifyLogo(): Painter

@Composable
expect fun SaavnLogo(): Painter

@Composable
expect fun SoundCloudLogo(): Painter

@Composable
expect fun YoutubeLogo(): Painter

@Composable
expect fun GaanaLogo(): Painter

@Composable
expect fun YoutubeMusicLogo(): Painter

@Composable
expect fun GithubLogo(): Painter

@Composable
expect fun PaypalLogo(): Painter

@Composable
expect fun OpenCollectiveLogo(): Painter

@Composable
expect fun RazorPay(): Painter

@Composable
expect fun HeartIcon(): Painter

@Composable
expect fun DownloadImageError(modifier: Modifier)

@Composable
expect fun DownloadImageArrow(modifier: Modifier)
