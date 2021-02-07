package com.shabinder.common.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource

@Composable
actual fun DownloadImageTick(modifier: Modifier){
    Image(
        vectorResource(R.drawable.ic_tick),
        "Download Done",
        modifier
    )
}

@Composable
actual fun DownloadImageError(modifier: Modifier){
    Image(
        vectorResource(R.drawable.ic_error),
        "Error! Cant Download this track",
        modifier
    )
}

@Composable
actual fun DownloadImageArrow(modifier: Modifier){
    Image(
        vectorResource(R.drawable.ic_arrow),
        "Start Download",
        modifier
    )
}

@Composable
actual fun DownloadAllImage() = vectorResource(R.drawable.ic_download_arrow)

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