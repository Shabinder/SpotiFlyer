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

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.database.DownloadRecord
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.gaana.GaanaTrack
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.networking.GaanaInterface
import com.shabinder.spotiflyer.ui.base.tracklistbase.TrackListViewModel
import com.shabinder.spotiflyer.utils.Provider
import com.shabinder.spotiflyer.utils.finalOutputDir
import com.shabinder.spotiflyer.utils.queryActiveTracks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GaanaViewModel @ViewModelInject constructor(
    val databaseDAO: DatabaseDAO,
    val gaanaInterface : GaanaInterface
) : TrackListViewModel(){

    override var folderType:String = ""
    override var subFolder:String = ""

    private val gaanaPlaceholderImageUrl = "https://a10.gaanacdn.com/images/social/gaana_social.jpg"

    fun gaanaSearch(type:String,link:String){
        viewModelScope.launch {
            when (type) {
                "song" -> {
                    gaanaInterface.getGaanaSong(seokey = link).value?.tracks?.firstOrNull()?.also {
                        folderType = "Tracks"
                        if (File(
                                finalOutputDir(
                                    it.track_title,
                                    folderType,
                                    subFolder
                                )
                            ).exists()
                        ) {//Download Already Present!!
                            it.downloaded = DownloadStatus.Downloaded
                        }
                        trackList.value = listOf(it).toTrackDetailsList()
                        title.value = it.track_title
                        coverUrl.value = it.artworkLink
                        withContext(Dispatchers.IO) {
                            databaseDAO.insert(
                                DownloadRecord(
                                    type = "Track",
                                    name = title.value!!,
                                    link = "https://gaana.com/$type/$link",
                                    coverUrl = coverUrl.value!!,
                                    totalFiles = 1,
                                    downloaded = it.downloaded == DownloadStatus.Downloaded,
                                    directory = finalOutputDir(
                                        it.track_title,
                                        folderType,
                                        subFolder
                                    )
                                )
                            )
                        }
                    }
                }
                "album" -> {
                    gaanaInterface.getGaanaAlbum(seokey = link).value?.also {
                        folderType = "Albums"
                        subFolder = link
                        it.tracks.forEach { track ->
                            if (File(
                                    finalOutputDir(
                                        track.track_title,
                                        folderType,
                                        subFolder
                                    )
                                ).exists()
                            ) {//Download Already Present!!
                                track.downloaded = DownloadStatus.Downloaded
                            }
                        }
                        trackList.value = it.tracks.toTrackDetailsList()
                        title.value = link
                        coverUrl.value = it.custom_artworks.size_480p
                        withContext(Dispatchers.IO) {
                            databaseDAO.insert(
                                DownloadRecord(
                                    type = "Album",
                                    name = title.value!!,
                                    link = "https://gaana.com/$type/$link",
                                    coverUrl = coverUrl.value.toString(),
                                    totalFiles = trackList.value?.size ?: 0,
                                    downloaded = File(
                                        finalOutputDir(
                                            type = folderType,
                                            subFolder = subFolder
                                        )
                                    ).listFiles()?.size == trackList.value?.size,
                                    directory = finalOutputDir(
                                        type = folderType,
                                        subFolder = subFolder
                                    )
                                )
                            )
                        }
                    }
                }
                "playlist" -> {
                    gaanaInterface.getGaanaPlaylist(seokey = link).value?.also {
                        folderType = "Playlists"
                        subFolder = link
                        it.tracks.forEach { track ->
                            if (File(
                                    finalOutputDir(
                                        track.track_title,
                                        folderType,
                                        subFolder
                                    )
                                ).exists()
                            ) {//Download Already Present!!
                                track.downloaded = DownloadStatus.Downloaded
                            }
                        }
                        trackList.value = it.tracks.toTrackDetailsList()
                        title.value = link
                        //coverUrl.value = "TODO"
                        coverUrl.value = gaanaPlaceholderImageUrl
                        withContext(Dispatchers.IO) {
                            databaseDAO.insert(
                                DownloadRecord(
                                    type = "Playlist",
                                    name = title.value.toString(),
                                    link = "https://gaana.com/$type/$link",
                                    coverUrl = coverUrl.value.toString(),
                                    totalFiles = it.tracks.size,
                                    downloaded = File(
                                        finalOutputDir(
                                            type = folderType,
                                            subFolder = subFolder
                                        )
                                    ).listFiles()?.size == trackList.value?.size,
                                    directory = finalOutputDir(
                                        type = folderType,
                                        subFolder = subFolder
                                    )
                                )
                            )
                        }
                    }
                }
                "artist" -> {
                    folderType = "Artist"
                    subFolder = link
                    val artistDetails =
                        gaanaInterface.getGaanaArtistDetails(seokey = link).value?.artist?.firstOrNull()
                            ?.also {
                                title.value = it.name
                                coverUrl.value = it.artworkLink
                            }
                    gaanaInterface.getGaanaArtistTracks(seokey = link).value?.also {
                        it.tracks.forEach { track ->
                            if (File(
                                    finalOutputDir(
                                        track.track_title,
                                        folderType,
                                        subFolder
                                    )
                                ).exists()
                            ) {//Download Already Present!!
                                track.downloaded = DownloadStatus.Downloaded
                            }
                        }
                        trackList.value = it.tracks.toTrackDetailsList()
                        withContext(Dispatchers.IO) {
                            databaseDAO.insert(
                                DownloadRecord(
                                    type = "Artist",
                                    name = artistDetails?.name ?: link,
                                    link = "https://gaana.com/$type/$link",
                                    coverUrl = coverUrl.value.toString(),
                                    totalFiles = trackList.value?.size ?: 0,
                                    downloaded = File(
                                        finalOutputDir(
                                            type = folderType,
                                            subFolder = subFolder
                                        )
                                    ).listFiles()?.size == trackList.value?.size,
                                    directory = finalOutputDir(
                                        type = folderType,
                                        subFolder = subFolder
                                    )
                                )
                            )
                        }
                    }
                }
            }
            queryActiveTracks()
        }
    }

    private fun List<GaanaTrack>.toTrackDetailsList() = this.map {
        TrackDetails(
            title = it.track_title,
            artists = it.artist.map { artist -> artist?.name.toString() },
            durationSec = it.duration,
            albumArt = File(
                Provider.imageDir + (it.artworkLink.substringBeforeLast('/').substringAfterLast('/')) + ".jpeg"),
            albumName = it.album_title,
            year = it.release_date,
            comment = "Genres:${it.genre?.map { genre -> genre?.name }?.reduceOrNull { acc, s -> acc + s  }}",
            trackUrl = it.lyrics_url,
            downloaded = it.downloaded ?: DownloadStatus.NotDownloaded,
            source = Source.Gaana,
            albumArtURL = it.artworkLink
        )
    }.toMutableList()
}