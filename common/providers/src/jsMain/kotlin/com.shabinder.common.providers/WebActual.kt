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
import com.shabinder.common.core_components.file_manager.FileManager
import com.shabinder.common.core_components.file_manager.allTracksStatus
import com.shabinder.common.core_components.file_manager.downloadFile
import com.shabinder.common.models.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

actual suspend fun downloadTracks(
    list: List<TrackDetails>,
    fetcher: FetchPlatformQueryResult,
    fileManager: FileManager
) {
    list.forEach { track ->
        withContext(dispatcherIO) {
            allTracksStatus[track.title] = DownloadStatus.Queued
            fetcher.findBestDownloadLink(track).fold(
                success = { res ->
                    track.audioQuality = res.second
                    downloadFile(res.first).collect {
                        when (it) {
                            is DownloadResult.Success -> {
                                println("Download Completed")
                                fileManager.saveFileWithMetadata(it.byteArray, track) {}
                            }
                            is DownloadResult.Error -> {
                                allTracksStatus[track.title] =
                                    DownloadStatus.Failed(it.cause ?: SpotiFlyerException.UnknownReason(it.cause))
                                println("Download Error: ${track.title}")
                            }
                            is DownloadResult.Progress -> {
                                allTracksStatus[track.title] = DownloadStatus.Downloading(it.progress)
                                println("Download Progress: ${it.progress}  : ${track.title}")
                            }
                        }
                        DownloadProgressFlow.emit(allTracksStatus)
                    }
                },
                failure = { error ->
                    allTracksStatus[track.title] = DownloadStatus.Failed(error)
                    DownloadProgressFlow.emit(allTracksStatus)
                }
            )
        }
    }
}
