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

package com.shabinder.spotiflyer.ui.gaana

import android.os.Environment
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.database.DownloadRecord
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.gaana.*
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.networking.GaanaInterface
import com.shabinder.spotiflyer.utils.BaseViewModel
import com.shabinder.spotiflyer.utils.Provider
import com.shabinder.spotiflyer.utils.finalOutputDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GaanaViewModel @ViewModelInject constructor(val databaseDAO: DatabaseDAO) : BaseViewModel(){

    override var folderType:String = ""
    override var subFolder:String = ""
    var gaanaInterface : GaanaInterface? = null

    fun gaanaSearch(type:String,link:String){
        when(type){
            "song" -> {
                uiScope.launch {
                    getGaanaSong(link)?.tracks?.firstOrNull()?.also {
                        folderType = "Tracks"
                        if(File(finalOutputDir(it.track_title,folderType,subFolder)).exists()){//Download Already Present!!
                            it.downloaded = DownloadStatus.Downloaded
                        }
                        trackList.value = listOf(it).toTrackDetailsList()
                        title.value = it.track_title
                        coverUrl.value = it.artworkLink
                        withContext(Dispatchers.IO){
                            databaseDAO.insert(
                                DownloadRecord(
                                type = "Track",
                                name = title.value!!,
                                link = "https://gaana.com/$type/$link",
                                coverUrl = coverUrl.value!!,
                                totalFiles = 1,
                                downloaded = it.downloaded == DownloadStatus.Downloaded,
                                directory = finalOutputDir(it.track_title,folderType,subFolder)
                            )
                            )
                        }
                    }
                }
            }
            "album" -> {
                uiScope.launch {
                    getGaanaAlbum(link)?.also {
                        folderType = "Albums"
                        subFolder = link
                        it.tracks.forEach { track ->
                            if(File(finalOutputDir(track.track_title,folderType,subFolder)).exists()){//Download Already Present!!
                                track.downloaded = DownloadStatus.Downloaded
                            }
                        }
                        trackList.value = it.tracks.toTrackDetailsList()
                        title.value = link
                        coverUrl.value = it.custom_artworks.size_480p
                        withContext(Dispatchers.IO){
                            databaseDAO.insert(DownloadRecord(
                                type = "Album",
                                name = title.value!!,
                                link = "https://gaana.com/$type/$link",
                                coverUrl = coverUrl.value.toString(),
                                totalFiles = trackList.value?.size ?: 0,
                                downloaded = File(finalOutputDir(type = folderType,subFolder = subFolder)).listFiles()?.size == trackList.value?.size,
                                directory = finalOutputDir(type = folderType,subFolder = subFolder)
                            ))
                        }
                    }
                }
            }
            "playlist" -> {
                uiScope.launch {
                    getGaanaPlaylist(link)?.also {
                        folderType = "Playlists"
                        subFolder = link
                        it.tracks.forEach {track ->
                            if(File(finalOutputDir(track.track_title,folderType,subFolder)).exists()){//Download Already Present!!
                                track.downloaded = DownloadStatus.Downloaded
                            }
                        }
                        trackList.value = it.tracks.toTrackDetailsList()
                        title.value = link
                        //coverUrl.value = "TODO"
                        withContext(Dispatchers.IO){
                            databaseDAO.insert(DownloadRecord(
                                type = "Playlist",
                                name = title.value.toString(),
                                link = "https://gaana.com/$type/$link",
                                coverUrl = coverUrl.value.toString(),
                                totalFiles = it.tracks.size,
                                downloaded = File(finalOutputDir(type = folderType,subFolder = subFolder)).listFiles()?.size == trackList.value?.size,
                                directory = finalOutputDir(type = folderType,subFolder = subFolder)
                            ))
                        }
                    }
                }
            }
            "artist" -> {
                uiScope.launch {
                    folderType = "Artist"
                    subFolder = link
                    val artistDetails = getGaanaArtistDetails(link)?.artist?.firstOrNull()?.also {
                        title.value = it.name
                        coverUrl.value = it.artworkLink
                    }
                    getGaanaArtistTracks(link)?.also {
                        it.tracks.forEach {track ->
                            if(File(finalOutputDir(track.track_title,folderType,subFolder)).exists()){//Download Already Present!!
                                track.downloaded = DownloadStatus.Downloaded
                            }
                        }
                        trackList.value = it.tracks.toTrackDetailsList()
                        withContext(Dispatchers.IO){
                            databaseDAO.insert(DownloadRecord(
                                type = "Artist",
                                name = artistDetails?.name ?: link,
                                link = "https://gaana.com/$type/$link",
                                coverUrl = coverUrl.value.toString(),
                                totalFiles = trackList.value?.size ?: 0,
                                downloaded = File(finalOutputDir(type = folderType,subFolder = subFolder)).listFiles()?.size == trackList.value?.size,
                                directory = finalOutputDir(type = folderType,subFolder = subFolder)
                            ))
                        }
                    }
                }
            }
        }
    }


    private fun List<GaanaTrack>.toTrackDetailsList() = this.map {
        TrackDetails(
            title = it.track_title,
            artists = it.artist.map { artist -> artist.name },
            durationSec = it.duration,
            albumArt = File(
                Environment.getExternalStorageDirectory(),
                Provider.defaultDir +".Images/" + (it.artworkLink.substringBeforeLast('/').substringAfterLast('/')) + ".jpeg"),
            albumName = it.album_title,
            year = it.release_date,
            comment = "Genres:${it.genre.map { genre -> genre.name }.reduceOrNull { acc, s -> acc + s  }}",
            trackUrl = it.lyrics_url,
            downloaded = it.downloaded ?: DownloadStatus.NotDownloaded,
            source = Source.Gaana,
            albumArtURL = it.artworkLink
        )
    }.toMutableList()

    private suspend fun getGaanaSong(songLink:String): GaanaSong?{
        Log.i("Requesting","https://gaana.com/song/$songLink")
        return gaanaInterface?.getGaanaSong(seokey =  songLink)?.value
    }
    private suspend fun getGaanaAlbum(albumLink:String): GaanaAlbum?{
        Log.i("Requesting","https://gaana.com/album/$albumLink")
        return gaanaInterface?.getGaanaAlbum(seokey =  albumLink)?.value
    }
    private suspend fun getGaanaPlaylist(link:String): GaanaPlaylist?{
        Log.i("Requesting","https://gaana.com/playlist/$link")
        return gaanaInterface?.getGaanaPlaylist(seokey = link)?.value
    }
    private suspend fun getGaanaArtistDetails(link:String): GaanaArtistDetails?{
        Log.i("Requesting","https://gaana.com/artist/$link")
        return gaanaInterface?.getGaanaArtistDetails(seokey = link)?.value
    }
    private suspend fun getGaanaArtistTracks(link:String,limit:Int = 50): GaanaArtistTracks?{
        Log.i("Requesting","Tracks of: https://gaana.com/artist/$link")
        return gaanaInterface?.getGaanaArtistTracks(seokey = link,limit = limit)?.value
    }
}