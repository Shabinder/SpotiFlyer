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
import androidx.compose.ui.graphics.vector.ImageVector
import com.shabinder.common.di.Picture

@Composable
expect fun ImageLoad(
    link: String,
    loader: suspend (String) -> Picture,
    desc: String = "Album Art",
    modifier: Modifier = Modifier,
    // placeholder:ImageVector = PlaceHolderImage()
)

@Composable
expect fun DownloadImageTick()

@Composable
expect fun DownloadAllImage(): ImageVector

@Composable
expect fun ShareImage(): ImageVector

@Composable
expect fun PlaceHolderImage(): ImageVector

@Composable
expect fun SpotiFlyerLogo(): ImageVector

@Composable
expect fun SpotifyLogo(): ImageVector

@Composable
expect fun YoutubeLogo(): ImageVector

@Composable
expect fun GaanaLogo(): ImageVector

@Composable
expect fun YoutubeMusicLogo(): ImageVector

@Composable
expect fun GithubLogo(): ImageVector

@Composable
expect fun HeartIcon(): ImageVector

@Composable
expect fun DownloadImageError()

@Composable
expect fun DownloadImageArrow(modifier: Modifier)
