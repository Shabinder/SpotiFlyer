package com.shabinder.common.di

import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.model.YoutubeVideo
import com.github.kiulian.downloader.model.formats.Format
import com.github.kiulian.downloader.model.quality.AudioQuality
import com.shabinder.common.models.DownloadResult
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

actual fun openPlatform(packageID:String, platformLink:String){
    //TODO
}

actual fun shareApp(){
    //TODO
}

actual fun giveDonation(){
    //TODO
}

val DownloadProgressFlow: MutableStateFlow<HashMap<String,DownloadStatus>> = MutableStateFlow(hashMapOf())

actual suspend fun downloadTracks(
    list: List<TrackDetails>,
    getYTIDBestMatch:suspend (String,TrackDetails)->String?,
    saveFileWithMetaData:suspend (mp3ByteArray:ByteArray, trackDetails: TrackDetails) -> Unit
){
    list.forEach {
        if (!it.videoID.isNullOrBlank()) {//Video ID already known!
            downloadTrack(it.videoID!!, it,saveFileWithMetaData)
        } else {
            val searchQuery = "${it.title} - ${it.artists.joinToString(",")}"
            val videoId = getYTIDBestMatch(searchQuery,it)
            if (videoId.isNullOrBlank()) {
                DownloadProgressFlow.emit(DownloadProgressFlow.value.apply { set(it.title,DownloadStatus.Failed) })
            } else {//Found Youtube Video ID
                downloadTrack(videoId, it,saveFileWithMetaData)
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
                        DownloadProgressFlow.emit(DownloadProgressFlow.value.apply { set(trackDetails.title,DownloadStatus.Failed) })
                    }
                    is DownloadResult.Progress -> {
                        DownloadProgressFlow.emit(DownloadProgressFlow.value.apply { set(trackDetails.title,DownloadStatus.Downloading(it.progress)) })
                    }
                    is DownloadResult.Success -> {//Todo clear map
                        saveFileWithMetaData(it.byteArray,trackDetails)
                        DownloadProgressFlow.emit(DownloadProgressFlow.value.apply { set(trackDetails.title,DownloadStatus.Downloaded) })
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