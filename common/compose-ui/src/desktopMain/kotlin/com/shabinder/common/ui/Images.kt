package com.shabinder.common.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorXmlResource

@Composable
actual fun DownloadImageTick(){
    Image(
        vectorXmlResource("common/compose-ui/src/main/res/drawable/ic_tick.xml"),
        "Downloaded"
    )
}

@Composable
actual fun DownloadImageError(){
    Image(
        vectorXmlResource("common/compose-ui/src/main/res/drawable/ic_error.xml"),
        "Can't Download"
    )
}

@Composable
actual fun DownloadImageArrow(modifier: Modifier){
    Image(
        vectorXmlResource("common/compose-ui/src/main/res/drawable/ic_arrow.xml"),
        "Download",
        modifier
    )
}

@Composable
actual fun DownloadAllImage():ImageVector = vectorXmlResource("common/compose-ui/src/main/res/drawable/ic_download_arrow.xml")

@Composable
actual fun PlaceHolderImage():ImageVector =    vectorXmlResource("common/compose-ui/src/main/res/drawable/music.xml")


@Composable
actual fun SpotiFlyerLogo():ImageVector = vectorXmlResource("common/compose-ui/src/main/res/drawable/ic_spotiflyer_logo.xml")

@Composable
actual fun HeartIcon():ImageVector = vectorXmlResource("common/compose-ui/src/main/res/drawable/ic_heart.xml")

@Composable
actual fun SpotifyLogo():ImageVector = vectorXmlResource("common/compose-ui/src/main/res/drawable/ic_spotify_logo.xml")

@Composable
actual fun YoutubeLogo():ImageVector = vectorXmlResource("common/compose-ui/src/main/res/drawable/ic_youtube.xml")

@Composable
actual fun GaanaLogo():ImageVector = vectorXmlResource("common/compose-ui/src/main/res/drawable/ic_gaana.xml")

@Composable
actual fun YoutubeMusicLogo():ImageVector = vectorXmlResource("common/compose-ui/src/main/res/drawable/ic_youtube_music_logo.xml")

@Composable
actual fun GithubLogo():ImageVector = vectorXmlResource("common/compose-ui/src/main/res/drawable/ic_github.xml")
