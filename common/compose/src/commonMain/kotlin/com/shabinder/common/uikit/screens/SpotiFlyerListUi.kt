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

@file:Suppress("UNUSED_VARIABLE")

package com.shabinder.common.uikit.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.shabinder.common.core_components.picture.Picture
import com.shabinder.common.list.SpotiFlyerList
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.Actions
import com.shabinder.common.translations.Strings
import com.shabinder.common.uikit.DownloadAllImage
import com.shabinder.common.uikit.DownloadImageArrow
import com.shabinder.common.uikit.DownloadImageError
import com.shabinder.common.uikit.DownloadImageTick
import com.shabinder.common.uikit.ImageLoad
import com.shabinder.common.uikit.VerticalScrollbar
import com.shabinder.common.uikit.configurations.SpotiFlyerTypography
import com.shabinder.common.uikit.configurations.appNameStyle
import com.shabinder.common.uikit.configurations.colorAccent
import com.shabinder.common.uikit.configurations.colorPrimary
import com.shabinder.common.uikit.configurations.lightGray
import com.shabinder.common.uikit.dialogs.DonationDialogComponent
import com.shabinder.common.uikit.dialogs.ErrorInfoDialog
import com.shabinder.common.uikit.rememberScrollbarAdapter

@Composable
fun SpotiFlyerListContent(
    component: SpotiFlyerList,
    modifier: Modifier = Modifier
) {
    val model by component.model.subscribeAsState()

    LaunchedEffect(model.errorOccurred) {
        /*Handle if Any Exception Occurred*/
        model.errorOccurred?.let {
            Actions.instance.showPopUpMessage(it.message ?: Strings.errorOccurred())
            component.onBackPressed()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        val result = model.queryResult
        if (result == null) {
            /* Loading Bar */
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier.padding(8.dp))
                Text("${Strings.loading()}...", style = appNameStyle, color = colorPrimary)
            }
        } else {

            val listState = rememberLazyListState()

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = {
                    item {
                        CoverImage(result.title, result.coverUrl, component::loadImage)
                    }
                    itemsIndexed(model.trackList) { _, item ->
                        TrackCard(
                            track = item,
                            downloadTrack = { component.onDownloadClicked(item) },
                            loadImage = { component.loadImage(item.albumArtURL) }
                        )
                    }
                },
                state = listState,
                modifier = Modifier.fillMaxSize(),
            )

            // Donation Dialog Visibility
            val (openDonationDialog, dismissDonationDialog, snoozeDonationDialog) = DonationDialogComponent {
                component.dismissDonationDialogSetOffset()
            }

            DownloadAllButton(
                onClick = {
                    component.onDownloadAllClicked(model.trackList)
                    // Check If we are allowed to show donation Dialog
                    if (model.askForDonation) {
                        // Show Donation Dialog
                        openDonationDialog()
                    }
                },
                modifier = Modifier.padding(bottom = 24.dp).align(Alignment.BottomCenter)
            )

            VerticalScrollbar(
                modifier = Modifier.padding(end = 2.dp).align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(
                    scrollState = listState,
                    itemCount = model.trackList.size,
                    averageItemSize = 72.dp
                )
            )
        }
    }
}

@Composable
fun TrackCard(
    track: TrackDetails,
    downloadTrack: () -> Unit,
    loadImage: suspend () -> Picture
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        ImageLoad(
            track.albumArtURL,
            { loadImage() },
            Strings.albumArt(),
            modifier = Modifier
                .width(70.dp)
                .height(70.dp)
                .clip(MaterialTheme.shapes.medium)
        )
        Column(modifier = Modifier.padding(horizontal = 8.dp).height(60.dp).weight(1f), verticalArrangement = Arrangement.SpaceEvenly) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = SpotiFlyerTypography.h6, color = colorAccent)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(horizontal = 8.dp).fillMaxSize()
            ) {
                Text("${track.artists.firstOrNull()}...", fontSize = 12.sp, maxLines = 1)
                Text("${track.durationSec / 60} ${Strings.minute()}, ${track.durationSec % 60} ${Strings.second()}", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        when (track.downloaded) {
            is DownloadStatus.Downloaded -> {
                DownloadImageTick()
            }
            is DownloadStatus.Queued -> {
                CircularProgressIndicator()
            }
            is DownloadStatus.Failed -> {
                val (openErrorDialog, dismissErrorDialog) = ErrorInfoDialog((track.downloaded as DownloadStatus.Failed).error)

                Icon(
                    Icons.Rounded.Info, Strings.downloadError(), tint = lightGray,
                    modifier = Modifier.size(42.dp).clickable {
                        openErrorDialog()
                    }.padding(start = 4.dp, end = 12.dp)
                )

                DownloadImageError(
                    Modifier.clickable(
                        onClick = {
                            downloadTrack()
                        }
                    )
                )
            }
            is DownloadStatus.Downloading -> {
                CircularProgressIndicator(progress = (track.downloaded as DownloadStatus.Downloading).progress.toFloat() / 100f)
            }
            is DownloadStatus.Converting -> {
                CircularProgressIndicator(progress = 100f, color = colorAccent)
            }
            is DownloadStatus.NotDownloaded -> {
                DownloadImageArrow(
                    Modifier.clickable(
                        onClick = {
                            downloadTrack()
                        }
                    )
                )
            }
        }
    }
}

@Composable
fun CoverImage(
    title: String,
    coverURL: String,
    loadImage: suspend (URL: String, isCover: Boolean) -> Picture,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.padding(vertical = 8.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ImageLoad(
            coverURL,
            { loadImage(coverURL, true) },
            Strings.coverImage(),
            modifier = Modifier
                .padding(12.dp)
                .width(190.dp)
                .height(210.dp)
                .clip(MaterialTheme.shapes.medium)
        )
        Text(
            text = title,
            style = SpotiFlyerTypography.h5,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
        )
    }
    /*scope.launch {
        updateGradient(coverURL, ctx)
    }*/
}

@Composable
fun DownloadAllButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    ExtendedFloatingActionButton(
        text = { Text(Strings.downloadAll()) },
        onClick = onClick,
        icon = { Icon(DownloadAllImage(), Strings.downloadAll() + Strings.button(), tint = Color(0xFF000000)) },
        backgroundColor = colorAccent,
        modifier = modifier
    )
}
