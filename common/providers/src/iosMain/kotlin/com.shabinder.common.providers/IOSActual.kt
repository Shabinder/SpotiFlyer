package com.shabinder.common.di

import com.shabinder.common.di.utils.ParallelExecutor
import com.shabinder.common.models.AllPlatforms
import com.shabinder.common.models.DownloadResult
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.Actions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect

@SharedImmutable
actual val dispatcherIO = Dispatchers.Default

@SharedImmutable
actual val currentPlatform: AllPlatforms = AllPlatforms.Native

@SharedImmutable
val Downloader = ParallelExecutor(dispatcherIO)

actual suspend fun downloadTracks(
    list: List<TrackDetails>,
    fetcher: FetchPlatformQueryResult,
    dir: Dir
) {
    dir.logger.i { "Downloading ${list.size} Tracks" }
    for (track in list) {
        Downloader.execute {
            val url = fetcher.findMp3DownloadLink(track)
            if (!url.isNullOrBlank()) { // Successfully Grabbed Mp3 URL
                downloadFile(url).collect {
                    fetcher.dir.logger.d { it.toString() }
                    /*Construct a `NEW Map` from frozen Map to Modify for Native Platforms*/
                    val map: MutableMap<String, DownloadStatus> = when (it) {
                        is DownloadResult.Error -> {
                            DownloadProgressFlow.replayCache.getOrElse(
                                0
                            ) { hashMapOf() }.toMutableMap().apply {
                                set(track.title, DownloadStatus.Failed)
                            }
                        }
                        is DownloadResult.Progress -> {
                            DownloadProgressFlow.replayCache.getOrElse(
                                0
                            ) { hashMapOf() }.toMutableMap().apply {
                                set(track.title, DownloadStatus.Downloading(it.progress))
                            }
                        }
                        is DownloadResult.Success -> { // Todo clear map
                            dir.saveFileWithMetadata(it.byteArray, track, Actions.instance::writeMp3Tags)
                            DownloadProgressFlow.replayCache.getOrElse(
                                0
                            ) { hashMapOf() }.toMutableMap().apply {
                                set(track.title, DownloadStatus.Downloaded)
                            }
                        }
                        else -> { mutableMapOf() }
                    }
                    DownloadProgressFlow.emit(
                        map as HashMap<String, DownloadStatus>
                    )
                }
            } else {
                DownloadProgressFlow.emit(
                    DownloadProgressFlow.replayCache.getOrElse(
                        0
                    ) { hashMapOf() }.apply { set(track.title, DownloadStatus.Failed) }
                )
            }
        }
    }
}

@SharedImmutable
val DownloadProgressFlow: MutableSharedFlow<HashMap<String, DownloadStatus>> = MutableSharedFlow(1)
