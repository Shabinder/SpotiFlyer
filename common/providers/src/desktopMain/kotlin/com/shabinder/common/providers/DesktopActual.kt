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

package com.shabinder.common.providers

import com.shabinder.common.core_components.file_manager.DownloadProgressFlow
import com.shabinder.common.core_components.file_manager.DownloadScope
import com.shabinder.common.core_components.file_manager.FileManager
import com.shabinder.common.core_components.file_manager.downloadFile
import com.shabinder.common.models.DownloadResult
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.SpotiFlyerException
import com.shabinder.common.models.TrackDetails
import kotlinx.coroutines.flow.collect

actual suspend fun downloadTracks(
    list: List<TrackDetails>,
    fetcher: FetchPlatformQueryResult,
    fileManager: FileManager
) {
    list.forEach { trackDetails ->
        DownloadScope.executeSuspending { // Send Download to Pool.
            fetcher.findBestDownloadLink(trackDetails).fold(
                success = { res ->
                    trackDetails.audioQuality = res.second
                    downloadFile(res.first).collect {
                        when (it) {
                            is DownloadResult.Error -> {
                                DownloadProgressFlow.emit(
                                    DownloadProgressFlow.replayCache.getOrElse(
                                        0
                                    ) { hashMapOf() }.apply {
                                        set(
                                            trackDetails.title,
                                            DownloadStatus.Failed(
                                                it.cause ?: SpotiFlyerException.UnknownReason(it.cause)
                                            )
                                        )
                                    }
                                )
                            }
                            is DownloadResult.Progress -> {
                                DownloadProgressFlow.emit(
                                    DownloadProgressFlow.replayCache.getOrElse(
                                        0
                                    ) { hashMapOf() }
                                        .apply { set(trackDetails.title, DownloadStatus.Downloading(it.progress)) }
                                )
                            }
                            is DownloadResult.Success -> { // Todo clear map
                                DownloadProgressFlow.emit(
                                    DownloadProgressFlow.replayCache.getOrElse(
                                        0
                                    ) { hashMapOf() }.apply { set(trackDetails.title, DownloadStatus.Converting) }
                                )
                                fileManager.saveFileWithMetadata(it.byteArray, trackDetails).fold(
                                    failure = {
                                        DownloadProgressFlow.emit(
                                            DownloadProgressFlow.replayCache.getOrElse(
                                                0
                                            ) { hashMapOf() }.apply { set(trackDetails.title, DownloadStatus.Failed(it)) }
                                        )
                                    },
                                    success = {
                                        DownloadProgressFlow.emit(
                                            DownloadProgressFlow.replayCache.getOrElse(
                                                0
                                            ) { hashMapOf() }.apply { set(trackDetails.title, DownloadStatus.Downloaded) }
                                        )
                                    }
                                )
                            }
                        }
                    }
                },
                failure = { error ->
                    DownloadProgressFlow.emit(
                        DownloadProgressFlow.replayCache.getOrElse(
                            0
                        ) { hashMapOf() }.apply { set(trackDetails.title, DownloadStatus.Failed(error)) }
                    )
                }
            )
        }
    }
}
