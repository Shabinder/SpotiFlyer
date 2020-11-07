/*
 * Copyright (C)  2020  Shabinder Singh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer.ui.youtube

import android.annotation.SuppressLint
import android.os.Environment
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.model.formats.Format
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.database.DownloadRecord
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.Source
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.utils.Provider
import com.shabinder.spotiflyer.utils.Provider.defaultDir
import com.shabinder.spotiflyer.utils.finalOutputDir
import com.shabinder.spotiflyer.utils.removeIllegalChars
import kotlinx.coroutines.*
import java.io.File

class YoutubeViewModel @ViewModelInject constructor(val databaseDAO: DatabaseDAO) : ViewModel(){

    /*
    * YT Album Art Schema
    * Normal Url: https://i.ytimg.com/vi/$searchId/maxresdefault.jpg"
    * */

    val ytTrackList = MutableLiveData<List<TrackDetails>>()
    val format = MutableLiveData<Format>()
    private val loading = "Loading"
    var title = MutableLiveData<String>().apply { value = "\"Loading!\"" }
    var coverUrl = MutableLiveData<String>().apply { value = loading }
    val folderType = "YT_Downloads"
    var subFolder = ""
    private var viewModelJob = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    fun getYTPlaylist(searchId:String, ytDownloader:YoutubeDownloader){
        uiScope.launch(Dispatchers.IO) {
            Log.i("YT Playlist",searchId)
            val playlist = ytDownloader.getPlaylist(searchId)
            val playlistDetails = playlist.details()
            val name = playlistDetails.title()
            subFolder = removeIllegalChars(name).toString()
            val videos = playlist.videos()
            coverUrl.postValue("https://i.ytimg.com/vi/${videos.firstOrNull()?.videoId()}/maxresdefault.jpg")
            title.postValue(
                if(name.length > 17){"${name.subSequence(0,16)}..."}else{name}
            )
            ytTrackList.postValue(videos.map {
                TrackDetails(
                    title = it.title(),
                    artists = listOf(it.author().toString()),
                    durationSec = it.lengthSeconds(),
                    albumArt = File(
                        Environment.getExternalStorageDirectory(),
                        defaultDir +".Images/" + it.videoId() + ".jpeg"
                    ),
                    source = Source.YouTube,
                    downloaded = if(File(finalOutputDir(itemName = removeIllegalChars(name),type = folderType,subFolder = subFolder)).exists())
                        DownloadStatus.Downloaded
                    else DownloadStatus.NotDownloaded
                )
            })

                withContext(Dispatchers.IO){
                databaseDAO.insert(DownloadRecord(
                    type = "PlayList",
                    name = if(name.length > 17){"${name.subSequence(0,16)}..."}else{name},
                    link = "https://www.youtube.com/playlist?list=$searchId",
                    coverUrl = "https://i.ytimg.com/vi/${videos.firstOrNull()?.videoId()}/maxresdefault.jpg",
                    totalFiles = videos.size,
                    directory = finalOutputDir(itemName = removeIllegalChars(name),type = folderType,subFolder = subFolder),
                    downloaded = File(finalOutputDir(itemName = removeIllegalChars(name),type = folderType,subFolder = subFolder)).exists()
                ))
            }
        }
    }

    @SuppressLint("DefaultLocale")
    fun getYTTrack(searchId:String, ytDownloader:YoutubeDownloader) {
        uiScope.launch(Dispatchers.IO) {
            Log.i("YT Video",searchId)
            val video = ytDownloader.getVideo(searchId)
            coverUrl.postValue("https://i.ytimg.com/vi/$searchId/maxresdefault.jpg")
            val detail = video?.details()
            val name = detail?.title()?.replace(detail.author()!!.toUpperCase(),"",true) ?: detail?.title() ?: ""
            Log.i("YT View Model",detail.toString())
            ytTrackList.postValue(listOf(TrackDetails(
                title = name,
                artists = listOf(detail?.author().toString()),
                durationSec = detail?.lengthSeconds()?:0,
                albumArt = File(
                    Environment.getExternalStorageDirectory(),
                    Provider.defaultDir +".Images/" + searchId + ".jpeg"
                ),
                source = Source.YouTube
            )))
            title.postValue(
                if(name.length > 17){"${name.subSequence(0,16)}..."}else{name}
            )

            withContext(Dispatchers.IO){
                databaseDAO.insert(DownloadRecord(
                    type = "Track",
                    name = if(name.length > 17){"${name.subSequence(0,16)}..."}else{name},
                    link = "https://www.youtube.com/watch?v=$searchId",
                    coverUrl = "https://i.ytimg.com/vi/$searchId/maxresdefault.jpg",
                    totalFiles = 1,
                    downloaded = false,
                    directory = finalOutputDir(type = "YT_Downloads")
                ))
            }
        }
    }
}

