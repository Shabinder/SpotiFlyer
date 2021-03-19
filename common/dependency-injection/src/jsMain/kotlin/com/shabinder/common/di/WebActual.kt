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

import com.shabinder.common.models.AllPlatforms
import com.shabinder.common.models.DownloadResult
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import io.ktor.client.request.head
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

actual val currentPlatform: AllPlatforms = AllPlatforms.Js

actual fun openPlatform(packageID: String, platformLink: String) {
    // TODO
}

actual fun shareApp() {
    // TODO
}

actual fun giveDonation() {
    // TODO
}

actual fun queryActiveTracks() {}

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

actual val isInternetAvailable: Boolean
    get() {
        return true
        /*var result = false
        val job = GlobalScope.launch { result = isInternetAvailable() }
        while(job.isActive){}
        return result*/
    }

val DownloadProgressFlow: MutableSharedFlow<HashMap<String, DownloadStatus>> = MutableSharedFlow(1)
// Error:https://github.com/Kotlin/kotlinx.atomicfu/issues/182
// val DownloadScope = ParallelExecutor(Dispatchers.Default) //Download Pool of 4 parallel
val allTracksStatus: HashMap<String, DownloadStatus> = hashMapOf()

actual suspend fun downloadTracks(
    list: List<TrackDetails>,
    fetcher: FetchPlatformQueryResult,
    dir: Dir
) {
    list.forEach {
        withContext(dispatcherIO) {
            allTracksStatus[it.title] = DownloadStatus.Queued
            if (!it.videoID.isNullOrBlank()) { // Video ID already known!
                downloadTrack(it.videoID!!, it, fetcher, dir)
            } else {
                val searchQuery = "${it.title} - ${it.artists.joinToString(",")}"
                val videoID = fetcher.youtubeMusic.getYTIDBestMatch(searchQuery, it)
                println(videoID + " : " + it.title)
                if (videoID.isNullOrBlank()) {
                    allTracksStatus[it.title] = DownloadStatus.Failed
                    DownloadProgressFlow.emit(allTracksStatus)
                } else { // Found Youtube Video ID
                    downloadTrack(videoID, it, fetcher, dir)
                }
            }
            DownloadProgressFlow.emit(allTracksStatus)
        }
    }
}

suspend fun downloadTrack(videoID: String, track: TrackDetails, fetcher: FetchPlatformQueryResult, dir: Dir) {
    val url = fetcher.youtubeMp3.getMp3DownloadLink(videoID)
    if (url == null) {
        allTracksStatus[track.title] = DownloadStatus.Failed
        DownloadProgressFlow.emit(allTracksStatus)
        println("No URL to Download")
    } else {
        downloadFile(url).collect {
            when (it) {
                is DownloadResult.Success -> {
                    println("Download Completed")
                    dir.saveFileWithMetadata(it.byteArray, track)
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
    }
}
