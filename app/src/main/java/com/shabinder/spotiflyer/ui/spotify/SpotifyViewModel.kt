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

package com.shabinder.spotiflyer.ui.spotify

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.spotiflyer.models.Album
import com.shabinder.spotiflyer.models.Playlist
import com.shabinder.spotiflyer.models.Track
import com.shabinder.spotiflyer.utils.SpotifyService
import com.shabinder.spotiflyer.utils.finalOutputDir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class SpotifyViewModel: ViewModel() {

    var folderType:String = ""
    var subFolder:String = ""
    var trackList = MutableLiveData<MutableList<Track>>()
    private val loading = "Loading"
    var title = MutableLiveData<String>().apply { value = loading }
    var coverUrl = MutableLiveData<String>().apply { value = loading }
    var spotifyService : SpotifyService? = null
    var ytDownloader : YoutubeDownloader? = null

    private var viewModelJob = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    fun spotifySearch(type:String,link: String){
        when (type) {
            "track" -> {
                uiScope.launch {
                    val trackObject = getTrackDetails(link)
                    folderType = "Tracks"
                    val tempTrackList = mutableListOf<Track>()
                    if(File(finalOutputDir(trackObject?.name!!,folderType,subFolder)).exists()){//Download Already Present!!
                        trackObject.downloaded = "Downloaded"
                    }
                    tempTrackList.add(trackObject)
                    trackList.value = tempTrackList
                    title.value = trackObject.name
                    coverUrl.value = trackObject.album!!.images?.get(0)!!.url!!
                }
            }

            "album" -> {
                uiScope.launch {
                    val albumObject = getAlbumDetails(link)
                    folderType = "Albums"
                    subFolder = albumObject?.name!!
                    val tempTrackList = mutableListOf<Track>()
                    albumObject.tracks?.items?.forEach {
                        if(File(finalOutputDir(it.name!!,folderType,subFolder)).exists()){//Download Already Present!!
                            it.downloaded = "Downloaded"
                        }
                        tempTrackList.add(it)
                    }
                    trackList.value = tempTrackList
                    title.value = albumObject.name
                    coverUrl.value = albumObject.images?.get(0)!!.url!!
                }
            }

            "playlist" -> {
                uiScope.launch {
                    val playlistObject = getPlaylistDetails(link)
                    folderType = "Playlists"
                    subFolder = playlistObject?.name!!
                    val tempTrackList = mutableListOf<Track>()
                    playlistObject.tracks?.items?.forEach {
                        it.track?.let {
                            it1 -> if(File(finalOutputDir(it1.name!!,folderType,subFolder)).exists()){//Download Already Present!!
                            it1.downloaded = "Downloaded"
                            Log.i("ViewModel123","${it1.name} Downloaded")
                            }
                            tempTrackList.add(it1)
                        }
                    }
                    Log.i("ViewModel",tempTrackList.size.toString())
                    Log.i("ViewModel",playlistObject.tracks?.items?.size.toString())
                    trackList.value = tempTrackList
                    title.value = playlistObject.name
                    coverUrl.value =  playlistObject.images?.get(0)!!.url!!

                }
            }
            "episode" -> {//TODO
            }
            "show" -> {//TODO
            }
        }
    }

    private suspend fun getTrackDetails(trackLink:String): Track?{
        Log.i("Requesting","https://api.spotify.com/v1/tracks/$trackLink")
        return spotifyService?.getTrack(trackLink)
    }
    private suspend fun getAlbumDetails(albumLink:String): Album?{
        Log.i("Requesting","https://api.spotify.com/v1/albums/$albumLink")
        return spotifyService?.getAlbum(albumLink)
    }
    private suspend fun getPlaylistDetails(link:String): Playlist?{
        Log.i("Requesting","https://api.spotify.com/v1/playlists/$link")
        return spotifyService?.getPlaylist(link)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}