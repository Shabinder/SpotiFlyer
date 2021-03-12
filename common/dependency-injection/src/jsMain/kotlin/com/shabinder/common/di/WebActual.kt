package com.shabinder.common.di

import com.shabinder.common.models.DownloadResult
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import org.khronos.webgl.ArrayBuffer

actual fun openPlatform(packageID:String, platformLink:String){
    //TODO
}

actual fun shareApp(){
    //TODO
}

actual fun giveDonation(){
    //TODO
}

actual fun queryActiveTracks(){}

actual val dispatcherIO: CoroutineDispatcher = Dispatchers.Default

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
        return true
        var result = false
        val job = GlobalScope.launch { result = isInternetAvailable() }
        while(job.isActive){}
        return result
    }

val DownloadProgressFlow: MutableSharedFlow<HashMap<String, DownloadStatus>> = MutableSharedFlow(1)

actual suspend fun downloadTracks(
    list: List<TrackDetails>,
    fetcher: FetchPlatformQueryResult,
    dir: Dir
){
    withContext(Dispatchers.Default){
        list.forEach {
            if (!it.videoID.isNullOrBlank()) {//Video ID already known!
                downloadTrack(it.videoID!!, it, fetcher, dir)
            } else {
                val searchQuery = "${it.title} - ${it.artists.joinToString(",")}"
                val videoID = fetcher.youtubeMusic.getYTIDBestMatch(searchQuery,it)
                if (videoID.isNullOrBlank()) {
                } else {//Found Youtube Video ID
                    downloadTrack(videoID, it, fetcher, dir)
                }
            }
        }
    }
}

suspend fun downloadTrack(videoID: String, track: TrackDetails, fetcher:FetchPlatformQueryResult,dir:Dir) {
    val url = fetcher.youtubeMp3.getMp3DownloadLink(videoID)
    if(url == null){
        // TODO Handle
        println("No URL to Download")
    }else {
        downloadFile(url).collect {
            when(it){
                is DownloadResult.Success -> {
                    println("Download Completed")
                   dir.saveFileWithMetadata(it.byteArray, track)
                }
                is DownloadResult.Error -> println("Download Error: ${track.title}")
                is DownloadResult.Progress -> println("Download Progress: ${it.progress}  : ${track.title}")
            }
        }
    }
}
