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

package com.shabinder.common.di

import com.shabinder.common.di.utils.ParallelExecutor
import com.shabinder.common.models.DownloadResult
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import com.shabinder.downloader.YoutubeDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect

val DownloadProgressFlow: MutableSharedFlow<HashMap<String, DownloadStatus>> = MutableSharedFlow(1)

// Scope Allowing 4 Parallel Downloads
val DownloadScope = ParallelExecutor(Dispatchers.IO)

actual suspend fun downloadTracks(
    list: List<TrackDetails>,
    fetcher: FetchPlatformQueryResult,
    dir: Dir
) {
    list.forEach {
        DownloadScope.execute { // Send Download to Pool.
            if (!it.videoID.isNullOrBlank()) { // Video ID already known!
                downloadTrack(it.videoID!!, it, dir::saveFileWithMetadata)
            } else {
                val searchQuery = "${it.title} - ${it.artists.joinToString(",")}"
                val videoId = fetcher.youtubeMusic.getYTIDBestMatch(searchQuery, it)
                if (videoId.isNullOrBlank()) {
                    DownloadProgressFlow.emit(
                        DownloadProgressFlow.replayCache.getOrElse(
                            0
                        ) { hashMapOf() }.apply { set(it.title, DownloadStatus.Failed) }
                    )
                } else { // Found Youtube Video ID
                    downloadTrack(videoId, it, dir::saveFileWithMetadata)
                }
            }
        }
    }
}

private val ytDownloader = YoutubeDownloader()

suspend fun downloadTrack(
    videoID: String,
    trackDetails: TrackDetails,
    saveFileWithMetaData: suspend (mp3ByteArray: ByteArray, trackDetails: TrackDetails) -> Unit
) {
    try {
        val audioData = ytDownloader.getVideo(videoID).getData()

        audioData?.let { format ->
            val url = format.url ?: return
            downloadFile(url).collect {
                when (it) {
                    is DownloadResult.Error -> {
                        DownloadProgressFlow.emit(
                            DownloadProgressFlow.replayCache.getOrElse(
                                0
                            ) { hashMapOf() }.apply { set(trackDetails.title, DownloadStatus.Failed) }
                        )
                    }
                    is DownloadResult.Progress -> {
                        DownloadProgressFlow.emit(
                            DownloadProgressFlow.replayCache.getOrElse(
                                0
                            ) { hashMapOf() }.apply { set(trackDetails.title, DownloadStatus.Downloading(it.progress)) }
                        )
                    }
                    is DownloadResult.Success -> { // Todo clear map
                        saveFileWithMetaData(it.byteArray, trackDetails)
                        DownloadProgressFlow.emit(
                            DownloadProgressFlow.replayCache.getOrElse(
                                0
                            ) { hashMapOf() }.apply { set(trackDetails.title, DownloadStatus.Downloaded) }
                        )
                    }
                }
            }
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}
