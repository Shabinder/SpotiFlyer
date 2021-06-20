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

import com.shabinder.common.models.DownloadResult
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

val DownloadProgressFlow: MutableSharedFlow<HashMap<String, DownloadStatus>> = MutableSharedFlow(1)
// Error:https://github.com/Kotlin/kotlinx.atomicfu/issues/182
// val DownloadScope = ParallelExecutor(Dispatchers.Default) //Download Pool of 4 parallel
val allTracksStatus: HashMap<String, DownloadStatus> = hashMapOf()

// IO-Dispatcher
actual val dispatcherIO: CoroutineDispatcher = Dispatchers.Default

actual suspend fun downloadTracks(
    list: List<TrackDetails>,
    fetcher: FetchPlatformQueryResult,
    dir: Dir
) {
    list.forEach { track ->
        withContext(dispatcherIO) {
            allTracksStatus[track.title] = DownloadStatus.Queued
            val url = fetcher.findMp3DownloadLink(track)
            if (!url.isNullOrBlank()) { // Successfully Grabbed Mp3 URL
                downloadFile(url).collect {
                    when (it) {
                        is DownloadResult.Success -> {
                            println("Download Completed")
                            dir.saveFileWithMetadata(it.byteArray, track) {}
                        }
                        is DownloadResult.Error -> {
                            allTracksStatus[track.title] = DownloadStatus.Failed
                            println("Download Error: ${track.title}")
                        }
                        is DownloadResult.Progress -> {
                            allTracksStatus[track.title] = DownloadStatus.Downloading(it.progress)
                            println("Download Progress: ${it.progress}  : ${track.title}")
                        }
                    }
                    DownloadProgressFlow.emit(allTracksStatus)
                }
            } else {
                allTracksStatus[track.title] = DownloadStatus.Failed
                DownloadProgressFlow.emit(allTracksStatus)
            }
        }
    }
}
