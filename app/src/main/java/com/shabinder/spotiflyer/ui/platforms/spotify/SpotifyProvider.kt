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

package com.shabinder.spotiflyer.ui.platforms.spotify

import androidx.annotation.WorkerThread
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.database.DownloadRecord
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.PlatformQueryResult
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.spotify.Album
import com.shabinder.spotiflyer.models.spotify.Image
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.models.spotify.Track
import com.shabinder.spotiflyer.networking.GaanaInterface
import com.shabinder.spotiflyer.networking.SpotifyService
import com.shabinder.spotiflyer.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun spotifySearch(
    type:String,
    link: String,
    spotifyService: SpotifyService,
    databaseDAO: DatabaseDAO
): PlatformQueryResult {
    val result = PlatformQueryResult(
        folderType = "",
        subFolder = "",
        title = "",
        coverUrl = "",
        trackList = listOf(),
    )
    with(result) {
        when (type) {
            "track" -> {
                spotifyService.getTrack(link).value?.also {
                    folderType = "Tracks"
                    subFolder = ""
                    if (File(
                            finalOutputDir(
                                it.name.toString(),
                                folderType,
                                subFolder
                            )
                        ).exists()
                    ) {//Download Already Present!!
                        it.downloaded = DownloadStatus.Downloaded
                    }
                    trackList = listOf(it).toTrackDetailsList(folderType, subFolder)
                    title = it.name.toString()
                    coverUrl = (it.album?.images?.elementAtOrNull(1)?.url
                        ?: it.album?.images?.elementAtOrNull(0)?.url).toString()
                    withContext(Dispatchers.IO) {
                        databaseDAO.insert(
                            DownloadRecord(
                                type = "Track",
                                name = title,
                                link = "https://open.spotify.com/$type/$link",
                                coverUrl = coverUrl,
                                totalFiles = 1,
                            )
                        )
                    }
                }
            }

            "album" -> {
                val albumObject = spotifyService.getAlbum(link).value
                folderType = "Albums"
                subFolder = albumObject?.name.toString()
                albumObject?.tracks?.items?.forEach {
                    if (File(
                            finalOutputDir(
                                it.name.toString(),
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
                albumObject?.tracks?.items?.toTrackDetailsList(folderType, subFolder).let {
                    if (it.isNullOrEmpty()) {
                        //TODO Handle Error
                        showDialog("Error Fetching Album")
                    } else {
                        trackList = it
                        title = albumObject?.name.toString()
                        coverUrl = (albumObject?.images?.elementAtOrNull(1)?.url
                            ?: albumObject?.images?.elementAtOrNull(0)?.url).toString()
                        withContext(Dispatchers.IO) {
                            databaseDAO.insert(
                                DownloadRecord(
                                    type = "Album",
                                    name = title,
                                    link = "https://open.spotify.com/$type/$link",
                                    coverUrl = coverUrl,
                                    totalFiles = trackList.size,
                                )
                            )
                        }
                    }
                }
            }

            "playlist" -> {
                log("Spotify Service", spotifyService.toString())
                val playlistObject = spotifyService.getPlaylist(link).value
                folderType = "Playlists"
                subFolder = playlistObject?.name.toString()
                val tempTrackList = mutableListOf<Track>()
                log("Tracks Fetched", playlistObject?.tracks?.items?.size.toString())
                playlistObject?.tracks?.items?.forEach {
                    it.track?.let { it1 ->
                        if (File(
                                finalOutputDir(
                                    it1.name.toString(),
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
                    val moreTracks =
                        spotifyService.getPlaylistTracks(link, offset = tempTrackList.size).value
                    moreTracks?.items?.forEach {
                        it.track?.let { it1 -> tempTrackList.add(it1) }
                    }
                    moreTracksAvailable = !moreTracks?.next.isNullOrBlank()
                }
                log("Total Tracks Fetched", tempTrackList.size.toString())
                trackList = tempTrackList.toTrackDetailsList(folderType, subFolder)
                title = playlistObject?.name.toString()
                coverUrl = playlistObject?.images?.elementAtOrNull(1)?.url
                    ?: playlistObject?.images?.firstOrNull()?.url.toString()
                withContext(Dispatchers.IO) {
                    databaseDAO.insert(
                        DownloadRecord(
                            type = "Playlist",
                            name = title,
                            link = "https://open.spotify.com/$type/$link",
                            coverUrl = coverUrl,
                            totalFiles = tempTrackList.size,
                        )
                    )
                }
            }
            "episode" -> {//TODO
            }
            "show" -> {//TODO
            }
            else -> {
                //TODO Handle Error
            }
        }
    }
    queryActiveTracks()
    return result
}

@WorkerThread
fun resolveLink(
    url:String,
    gaanaInterface: GaanaInterface
):String {
    val response = gaanaInterface.getResponse(url).execute().body()?.string().toString()
    val regex = """https://open\.spotify\.com.+\w""".toRegex()
    return regex.find(response)?.value.toString()
}

private fun List<Track>.toTrackDetailsList(type:String, subFolder:String) = this.map {
    TrackDetails(
        title = it.name.toString(),
        artists = it.artists?.map { artist -> artist?.name.toString() } ?: listOf(),
        durationSec = (it.duration_ms/1000).toInt(),
        albumArt = File(
            Provider.imageDir() + (it.album?.images?.elementAtOrNull(1)?.url ?: it.album?.images?.firstOrNull()?.url.toString()).substringAfterLast('/') + ".jpeg"),
        albumName = it.album?.name,
        year = it.album?.release_date,
        comment = "Genres:${it.album?.genres?.joinToString()}",
        trackUrl = it.href,
        downloaded = it.downloaded,
        source = Source.Spotify,
        albumArtURL = it.album?.images?.elementAtOrNull(1)?.url ?: it.album?.images?.firstOrNull()?.url.toString(),
        outputFile = finalOutputDir(it.name.toString(),type, subFolder,".m4a")
    )
}