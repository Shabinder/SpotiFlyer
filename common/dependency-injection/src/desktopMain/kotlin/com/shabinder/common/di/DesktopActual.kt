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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect

val DownloadProgressFlow: MutableSharedFlow<HashMap<String, DownloadStatus>> = MutableSharedFlow(1)

// Scope Allowing 4 Parallel Downloads
val DownloadScope = ParallelExecutor(Dispatchers.IO)

// IO-Dispatcher
actual val dispatcherIO: CoroutineDispatcher = Dispatchers.IO

actual suspend fun downloadTracks(
    list: List<TrackDetails>,
    fetcher: FetchPlatformQueryResult,
    dir: Dir
) {
    list.forEach { trackDetails ->
        DownloadScope.execute { // Send Download to Pool.
            val url = fetcher.findMp3DownloadLink(trackDetails)
            if (!url.isNullOrBlank()) { // Successfully Grabbed Mp3 URL
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
                            dir.saveFileWithMetadata(it.byteArray, trackDetails) {}
                            DownloadProgressFlow.emit(
                                DownloadProgressFlow.replayCache.getOrElse(
                                    0
                                ) { hashMapOf() }.apply { set(trackDetails.title, DownloadStatus.Downloaded) }
                            )
                        }
                    }
                }
            } else {
                DownloadProgressFlow.emit(
                    DownloadProgressFlow.replayCache.getOrElse(
                        0
                    ) { hashMapOf() }.apply { set(trackDetails.title, DownloadStatus.Failed) }
                )
            }
        }
    }
}
