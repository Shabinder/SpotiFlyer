package com.shabinder.common.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorXmlResource

@Composable
actual fun DownloadImageTick(modifier: Modifier){
    Image(
        vectorXmlResource("common/compose-ui/src/main/res/drawable/ic_tick.xml"),
        modifier
    )
}

@Composable
actual fun DownloadImageError(modifier: Modifier){
    Image(
        vectorXmlResource("common/compose-ui/src/main/res/drawable/ic_error.xml"),
        modifier
    )
}

@Composable
actual fun DownloadImageArrow(modifier: Modifier){
    Image(
        vectorXmlResource("common/compose-ui/src/main/res/drawable/ic_arrow.xml"),
        modifier
    )
}

@Composable
actual fun DownloadAllImage():ImageVector = vectorXmlResource("common/compose-ui/src/main/res/drawable/ic_download_arrow.xml")
