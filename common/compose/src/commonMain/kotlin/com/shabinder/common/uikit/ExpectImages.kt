@file:Suppress("FunctionName")
package com.shabinder.common.uikit

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.shabinder.common.di.Picture

@Composable
expect fun ImageLoad(
    link:String,
    loader:suspend (String) ->Picture,
    desc: String = "Album Art",
    modifier:Modifier = Modifier,
    //placeholder:ImageVector = PlaceHolderImage()
)

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