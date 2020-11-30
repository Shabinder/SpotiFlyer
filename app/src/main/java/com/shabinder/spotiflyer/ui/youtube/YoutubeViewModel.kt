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
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.database.DownloadRecord
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.ui.base.tracklistbase.TrackListViewModel
import com.shabinder.spotiflyer.utils.Provider.imageDir
import com.shabinder.spotiflyer.utils.finalOutputDir
import com.shabinder.spotiflyer.utils.isOnline
import com.shabinder.spotiflyer.utils.removeIllegalChars
import com.shabinder.spotiflyer.utils.showMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class YoutubeViewModel @ViewModelInject constructor(
    val databaseDAO: DatabaseDAO,
    private val ytDownloader: YoutubeDownloader
) : TrackListViewModel(){
    /*
    * YT Album Art Schema
    * HI-RES Url: https://i.ytimg.com/vi/$searchId/maxresdefault.jpg"
    * Normal Url: https://i.ytimg.com/vi/$searchId/hqdefault.jpg"
    * */

    override var folderType = "YT_Downloads"
    override var subFolder = ""

    fun getYTPlaylist(searchId:String){
        if(!isOnline())return
        try{
            viewModelScope.launch(Dispatchers.IO) {
                Log.i("YT Playlist",searchId)
                val playlist = ytDownloader.getPlaylist(searchId)
                val playlistDetails = playlist.details()
                val name = playlistDetails.title()
                subFolder = removeIllegalChars(name).toString()
                val videos = playlist.videos()
                coverUrl.postValue("https://i.ytimg.com/vi/${videos.firstOrNull()?.videoId()}/hqdefault.jpg")
                title.postValue(
                    if(name.length > 17){"${name.subSequence(0,16)}..."}else{name}
                )
                this@YoutubeViewModel.trackList.postValue(videos.map {
                    TrackDetails(
                        title = it.title(),
                        artists = listOf(it.author().toString()),
                        durationSec = it.lengthSeconds(),
                        albumArt = File(
                            imageDir + it.videoId() + ".jpeg"
                        ),
                        source = Source.YouTube,
                        albumArtURL = "https://i.ytimg.com/vi/${it.videoId()}/hqdefault.jpg",
                        downloaded = if (File(
                            finalOutputDir(
                                itemName = it.title(),
                                type = folderType,
                                subFolder = subFolder
                            )).exists()
                        )
                            DownloadStatus.Downloaded
                        else {
                            DownloadStatus.NotDownloaded
                        }
                    )
                }.toMutableList())

                withContext(Dispatchers.IO){
                    databaseDAO.insert(DownloadRecord(
                        type = "PlayList",
                        name = if(name.length > 17){"${name.subSequence(0,16)}..."}else{name},
                        link = "https://www.youtube.com/playlist?list=$searchId",
                        coverUrl = "https://i.ytimg.com/vi/${videos.firstOrNull()?.videoId()}/hqdefault.jpg",
                        totalFiles = videos.size,
                        directory = finalOutputDir(itemName = removeIllegalChars(name),type = folderType,subFolder = subFolder),
                        downloaded = File(finalOutputDir(itemName = removeIllegalChars(name),type = folderType,subFolder = subFolder)).exists()
                    ))
                }
            }
        }catch (e:com.github.kiulian.downloader.YoutubeException.BadPageException){
            showMessage("An Error Occurred While Processing!")
        }

    }

    @SuppressLint("DefaultLocale")
    fun getYTTrack(searchId:String) {
        if(!isOnline())return
        try{
            viewModelScope.launch(Dispatchers.IO) {
                Log.i("YT Video",searchId)
                val video = ytDownloader.getVideo(searchId)
                coverUrl.postValue("https://i.ytimg.com/vi/$searchId/hqdefault.jpg")
                val detail = video?.details()
                val name = detail?.title()?.replace(detail.author()!!.toUpperCase(),"",true) ?: detail?.title() ?: ""
                Log.i("YT View Model",detail.toString())
                this@YoutubeViewModel.trackList.postValue(
                    listOf(
                        TrackDetails(
                            title = name,
                            artists = listOf(detail?.author().toString()),
                            durationSec = detail?.lengthSeconds()?:0,
                            albumArt = File(imageDir,"$searchId.jpeg"),
                            source = Source.YouTube,
                            albumArtURL = "https://i.ytimg.com/vi/$searchId/hqdefault.jpg"
                    )
                    ).toMutableList()
                )
                title.postValue(
                    if(name.length > 17){"${name.subSequence(0,16)}..."}else{name}
                )

                withContext(Dispatchers.IO){
                    databaseDAO.insert(DownloadRecord(
                        type = "Track",
                        name = if(name.length > 17){"${name.subSequence(0,16)}..."}else{name},
                        link = "https://www.youtube.com/watch?v=$searchId",
                        coverUrl = "https://i.ytimg.com/vi/$searchId/hqdefault.jpg",
                        totalFiles = 1,
                        downloaded = false,
                        directory = finalOutputDir(type = "YT_Downloads")
                    ))
                }
            }
        } catch (e:com.github.kiulian.downloader.YoutubeException){
            showMessage("An Error Occurred While Processing!")
        }
    }
}

