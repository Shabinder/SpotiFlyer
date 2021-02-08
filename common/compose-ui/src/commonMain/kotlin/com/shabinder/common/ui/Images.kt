package com.shabinder.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
expect fun DownloadImageTick()

@Composable
expect fun DownloadAllImage():ImageVector

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