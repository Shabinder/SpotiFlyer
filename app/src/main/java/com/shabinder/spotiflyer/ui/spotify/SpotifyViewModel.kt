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
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.database.DownloadRecord
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.spotify.Album
import com.shabinder.spotiflyer.models.spotify.Image
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.models.spotify.Track
import com.shabinder.spotiflyer.networking.GaanaInterface
import com.shabinder.spotiflyer.networking.SpotifyService
import com.shabinder.spotiflyer.ui.base.tracklistbase.TrackListViewModel
import com.shabinder.spotiflyer.utils.Provider.imageDir
import com.shabinder.spotiflyer.utils.finalOutputDir
import com.shabinder.spotiflyer.utils.queryActiveTracks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SpotifyViewModel @ViewModelInject constructor(
    val databaseDAO: DatabaseDAO,
    val gaanaInterface : GaanaInterface
) : TrackListViewModel(){

    override var folderType:String = ""
    override var subFolder:String = ""

    var spotifyService : SpotifyService? = null

    fun resolveLink(url:String):String {
        val response = gaanaInterface.getResponse(url).execute().body()?.string().toString()
        val regex = """https://open\.spotify\.com.+\w""".toRegex()
        return regex.find(response)?.value.toString()
    }

    fun spotifySearch(type:String,link: String){
        viewModelScope.launch {
            when (type) {
                "track" -> {
                    spotifyService?.getTrack(link)?.value?.also {
                        folderType = "Tracks"
                        if (File(
                                finalOutputDir(
                                    it.name,
                                    folderType,
                                    subFolder
                                )
                            ).exists()
                        ) {//Download Already Present!!
                            it.downloaded = DownloadStatus.Downloaded
                        }
                        trackList.value = listOf(it).toTrackDetailsList()
                        title.value = it.name
                        coverUrl.value = it.album!!.images?.elementAtOrNull(1)?.url
                            ?: it.album!!.images?.elementAtOrNull(0)?.url
                        withContext(Dispatchers.IO) {
                            databaseDAO.insert(
                                DownloadRecord(
                                    type = "Track",
                                    name = title.value!!,
                                    link = "https://open.spotify.com/$type/$link",
                                    coverUrl = coverUrl.value!!,
                                    totalFiles = 1,
                                    downloaded = it.downloaded == DownloadStatus.Downloaded,
                                    directory = finalOutputDir(it.name, folderType, subFolder)
                                )
                            )
                        }
                    }
                }

                "album" -> {
                    val albumObject = spotifyService?.getAlbum(link)?.value
                    folderType = "Albums"
                    subFolder = albumObject?.name.toString()
                    albumObject?.tracks?.items?.forEach {
                        if (File(
                                finalOutputDir(
                                    it.name!!,
                                    folderType,
                                    subFolder
                                )
                            ).exists()
                        ) {//Download Already Present!!
                            it.downloaded = DownloadStatus.Downloaded
                        }
                        it.album = Album(
                            images = listOf(
                                Image(
                                    url = albumObject.images?.elementAtOrNull(1)?.url
                                        ?: albumObject.images?.elementAtOrNull(0)?.url
                                )
                            )
                        )
                    }
                    trackList.value = albumObject?.tracks?.items?.toTrackDetailsList()
                    title.value = albumObject?.name
                    coverUrl.value = albumObject?.images?.elementAtOrNull(1)?.url
                        ?: albumObject?.images?.elementAtOrNull(0)?.url
                    withContext(Dispatchers.IO) {
                        databaseDAO.insert(
                            DownloadRecord(
                                type = "Album",
                                name = title.value!!,
                                link = "https://open.spotify.com/$type/$link",
                                coverUrl = coverUrl.value.toString(),
                                totalFiles = trackList.value?.size ?: 0,
                                downloaded = File(
                                    finalOutputDir(
                                        type = folderType,
                                        subFolder = subFolder
                                    )
                                ).listFiles()?.size == trackList.value?.size,
                                directory = finalOutputDir(type = folderType, subFolder = subFolder)
                            )
                        )
                    }
                }

                "playlist" -> {
                    Log.i("Spotify Service",spotifyService.toString())
                    val playlistObject = spotifyService?.getPlaylist(link)?.value
                    folderType = "Playlists"
                    subFolder = playlistObject?.name.toString()
                    val tempTrackList = mutableListOf<Track>()
                    Log.i("Tracks Fetched", playlistObject?.tracks?.items?.size.toString())
                    playlistObject?.tracks?.items?.forEach {
                        it.track?.let { it1 ->
                            if (File(
                                    finalOutputDir(
                                        it1.name!!,
                                        folderType,
                                        subFolder
                                    )
                                ).exists()
                            ) {//Download Already Present!!
                                it1.downloaded = DownloadStatus.Downloaded
                            }
                            tempTrackList.add(it1)
                        }
                    }
                    var moreTracksAvailable = !playlistObject?.tracks?.next.isNullOrBlank()

                    while (moreTracksAvailable) {
                        //Check For More Tracks If available
                        val moreTracks = spotifyService?.getPlaylistTracks(link, offset = tempTrackList.size)?.value
                        moreTracks?.items?.forEach {
                            it.track?.let { it1 -> tempTrackList.add(it1) }
                        }
                        moreTracksAvailable = !moreTracks?.next.isNullOrBlank()
                    }
                    Log.i("Total Tracks Fetched", tempTrackList.size.toString())
                    trackList.value = tempTrackList.toTrackDetailsList()
                    title.value = playlistObject?.name
                    coverUrl.value = playlistObject?.images?.elementAtOrNull(1)?.url
                        ?: playlistObject?.images?.firstOrNull()?.url.toString()
                    withContext(Dispatchers.IO) {
                        databaseDAO.insert(
                            DownloadRecord(
                                type = "Playlist",
                                name = title.value.toString(),
                                link = "https://open.spotify.com/$type/$link",
                                coverUrl = coverUrl.value.toString(),
                                totalFiles = tempTrackList.size,
                                downloaded = File(
                                    finalOutputDir(
                                        type = folderType,
                                        subFolder = subFolder
                                    )
                                ).listFiles()?.size == tempTrackList.size,
                                directory = finalOutputDir(type = folderType, subFolder = subFolder)
                            )
                        )
                    }
                }
                "episode" -> {//TODO
                }
                "show" -> {//TODO
                }
            }
            queryActiveTracks()
        }
    }

    @Suppress("DEPRECATION")
    private fun List<Track>.toTrackDetailsList() = this.map {
        TrackDetails(
            title = it.name.toString(),
            artists = it.artists?.map { artist -> artist?.name.toString() } ?: listOf(),
            durationSec = (it.duration_ms/1000).toInt(),
            albumArt = File(
                imageDir + (it.album?.images?.elementAtOrNull(1)?.url ?: it.album?.images?.firstOrNull()?.url.toString()).substringAfterLast('/') + ".jpeg"),
            albumName = it.album?.name,
            year = it.album?.release_date,
            comment = "Genres:${it.album?.genres?.joinToString()}",
            trackUrl = it.href,
            downloaded = it.downloaded,
            source = Source.Spotify,
            albumArtURL = it.album?.images?.elementAtOrNull(1)?.url ?: it.album?.images?.firstOrNull()?.url.toString()
        )
    }.toMutableList()
}