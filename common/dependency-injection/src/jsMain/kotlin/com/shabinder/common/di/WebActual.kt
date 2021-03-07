package com.shabinder.common.di

import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow

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
    getYTIDBestMatch:suspend (String, TrackDetails)->String?,
    saveFileWithMetaData:suspend (mp3ByteArray:ByteArray, trackDetails: TrackDetails) -> Unit
){/*
    list.forEach {
        if (!it.videoID.isNullOrBlank()) {//Video ID already known!
        } else {

        }
    }*/
}
