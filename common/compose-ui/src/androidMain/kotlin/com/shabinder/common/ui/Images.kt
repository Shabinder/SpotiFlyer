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