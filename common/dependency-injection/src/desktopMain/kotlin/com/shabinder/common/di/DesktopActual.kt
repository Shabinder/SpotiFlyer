package com.shabinder.common.di

import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.model.YoutubeVideo
import com.github.kiulian.downloader.model.formats.Format
import com.github.kiulian.downloader.model.quality.AudioQuality
import com.shabinder.common.di.utils.ParallelExecutor
import com.shabinder.common.models.AllPlatforms
import com.shabinder.common.models.DownloadResult
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

actual fun openPlatform(packageID:String, platformLink:String){
    //TODO
}
actual val currentPlatform: AllPlatforms = AllPlatforms.Jvm

actual val dispatcherIO = Dispatchers.IO

actual fun shareApp(){
    //TODO
}

actual fun giveDonation(){
    //TODO
}

actual fun queryActiveTracks(){}

/*
* Refactor This
* */
private suspend fun isInternetAvailable(): Boolean {
    return withContext(dispatcherIO) {
        try {
            ktorHttpClient.head<String>("http://google.com")
            true
        } catch (e: Exception) {
            println(e.message)
            false
        }
    }
}

actual val isInternetAvailable:Boolean
    get(){
        var result = false
        val job = GlobalScope.launch { result = isInternetAvailable() }
        while(job.isActive){}
        return result
    }

val DownloadProgressFlow: MutableSharedFlow<HashMap<String,DownloadStatus>> = MutableSharedFlow(1)

//Scope Allowing 4 Parallel Downloads
val DownloadScope = ParallelExecutor(Dispatchers.IO)

actual suspend fun downloadTracks(
    list: List<TrackDetails>,
    fetcher: FetchPlatformQueryResult,
    dir: Dir
){
    list.forEach {
        DownloadScope.execute { // Send Download to Pool.
            if (!it.videoID.isNullOrBlank()) {//Video ID already known!
                downloadTrack(it.videoID!!, it,dir::saveFileWithMetadata)
            } else {
                val searchQuery = "${it.title} - ${it.artists.joinToString(",")}"
                val videoId = fetcher.youtubeMusic.getYTIDBestMatch(searchQuery,it)
                if (videoId.isNullOrBlank()) {
                    DownloadProgressFlow.emit(DownloadProgressFlow.replayCache.getOrElse(0
                    ) { hashMapOf() }.apply { set(it.title,DownloadStatus.Failed) })
                } else {//Found Youtube Video ID
                    downloadTrack(videoId, it,dir::saveFileWithMetadata)
                }
            }
        }
    }
}

private val ytDownloader = YoutubeDownloader()

suspend fun downloadTrack(
    videoID: String,
    trackDetails: TrackDetails,
    saveFileWithMetaData:suspend (mp3ByteArray:ByteArray, trackDetails: TrackDetails) -> Unit
) {
    try {
        val audioData = ytDownloader.getVideo(videoID).getData()

        audioData?.let { format ->
            val url: String = format.url()
            downloadFile(url).collect {
                when(it){
                    is DownloadResult.Error -> {
                        DownloadProgressFlow.emit(DownloadProgressFlow.replayCache.getOrElse(0
                        ) { hashMapOf() }.apply { set(trackDetails.title,DownloadStatus.Failed) })
                    }
                    is DownloadResult.Progress -> {
                        DownloadProgressFlow.emit(DownloadProgressFlow.replayCache.getOrElse(0
                        ) { hashMapOf() }.apply { set(trackDetails.title,DownloadStatus.Downloading(it.progress)) })
                    }
                    is DownloadResult.Success -> {//Todo clear map
                        saveFileWithMetaData(it.byteArray,trackDetails)
                        DownloadProgressFlow.emit(DownloadProgressFlow.replayCache.getOrElse(0
                        ) { hashMapOf() }.apply { set(trackDetails.title,DownloadStatus.Downloaded) })
                    }
                }
            }
        }
    }catch (e: java.lang.Exception){
        e.printStackTrace()
    }
}
fun YoutubeVideo.getData(): Format?{
    return try {
        findAudioWithQuality(AudioQuality.medium)?.get(0) as Format
    } catch (e: java.lang.IndexOutOfBoundsException) {
        try {
            findAudioWithQuality(AudioQuality.high)?.get(0) as Format
        } catch (e: java.lang.IndexOutOfBoundsException) {
            try {
                findAudioWithQuality(AudioQuality.low)?.get(0) as Format
            } catch (e: java.lang.IndexOutOfBoundsException) {
                null
            }
        }
    }
}