package com.shabinder.common.di

import com.shabinder.common.di.providers.getData
import com.shabinder.common.di.utils.ParallelExecutor
import com.shabinder.common.models.AllPlatforms
import com.shabinder.common.models.DownloadResult
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.methods
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
            if (!track.videoID.isNullOrBlank()) { // Video ID already known!
                dir.logger.i { "VideoID:  ${track.title} -> ${track.videoID}" }
                downloadTrack(track.videoID!!, track, dir::saveFileWithMetadata,fetcher)
            } else {
                val searchQuery = "${track.title} - ${track.artists.joinToString(",")}"
                val videoId = fetcher.youtubeMusic.getYTIDBestMatch(searchQuery, track)
                dir.logger.i { "VideoID:  ${track.title} -> $videoId" }
                if (videoId.isNullOrBlank()) {
                    DownloadProgressFlow.emit(
                        DownloadProgressFlow.replayCache.getOrElse(
                            0
                        ) { hashMapOf() }.apply { set(track.title, DownloadStatus.Failed) }
                    )
                } else { // Found Youtube Video ID
                    downloadTrack(videoId, track, dir::saveFileWithMetadata,fetcher)
                }
            }
        }
    }
}

@SharedImmutable
val DownloadProgressFlow: MutableSharedFlow<HashMap<String, DownloadStatus>> = MutableSharedFlow(1)

suspend fun downloadTrack(
    videoID: String,
    trackDetails: TrackDetails,
    saveFileWithMetaData: suspend (mp3ByteArray: ByteArray, trackDetails: TrackDetails, postProcess: (TrackDetails) -> Unit) -> Unit,
    fetcher: FetchPlatformQueryResult
) {
    try {
        var link = fetcher.youtubeMp3.getMp3DownloadLink(videoID)

        fetcher.dir.logger.i { "LINK: $videoID -> $link" }
        if (link == null) {
            link = fetcher.youtubeProvider.ytDownloader.getVideo(videoID).get()?.url ?: return
        }
        fetcher.dir.logger.i { "LINK: $videoID -> $link" }
        downloadFile(link).collect {
            fetcher.dir.logger.d { it.toString() }
            /*Construct a `NEW Map` from frozen Map to Modify for Native Platforms*/
            val map: MutableMap<String, DownloadStatus> = when (it) {
                is DownloadResult.Error -> {
                    DownloadProgressFlow.replayCache.getOrElse(
                        0
                    ) { hashMapOf() }.toMutableMap().apply {
                        set(trackDetails.title, DownloadStatus.Failed)
                    }
                }
                is DownloadResult.Progress -> {
                    DownloadProgressFlow.replayCache.getOrElse(
                        0
                    ) { hashMapOf() }.toMutableMap().apply {
                        set(trackDetails.title,DownloadStatus.Downloading(it.progress))
                    }
                }
                is DownloadResult.Success -> { // Todo clear map
                    saveFileWithMetaData(it.byteArray, trackDetails, methods.value::writeMp3Tags)
                    DownloadProgressFlow.replayCache.getOrElse(
                        0
                    ) { hashMapOf() }.toMutableMap().apply {
                        set(trackDetails.title, DownloadStatus.Downloaded)
                    }
                }
                else -> { mutableMapOf() }
            }
            DownloadProgressFlow.emit(
                map as HashMap<String, DownloadStatus>
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
