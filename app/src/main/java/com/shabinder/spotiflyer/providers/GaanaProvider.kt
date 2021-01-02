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

package com.shabinder.spotiflyer.providers

import android.content.Context
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.database.DownloadRecord
import com.shabinder.spotiflyer.di.DefaultDir
import com.shabinder.spotiflyer.di.ImageDir
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.PlatformQueryResult
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.gaana.GaanaTrack
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.networking.GaanaInterface
import com.shabinder.spotiflyer.utils.finalOutputDir
import com.shabinder.spotiflyer.utils.log
import com.shabinder.spotiflyer.utils.queryActiveTracks
import com.shabinder.spotiflyer.utils.showDialog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GaanaProvider @Inject constructor(
    @DefaultDir private val defaultDir: String,
    @ImageDir private val imageDir: String,
    private val gaanaInterface: GaanaInterface,
    private val databaseDAO: DatabaseDAO,
    @ApplicationContext private val ctx : Context
){
    private val gaanaPlaceholderImageUrl = "https://a10.gaanacdn.com/images/social/gaana_social.jpg"

    suspend fun queryGaana(
        fullLink: String,
    ):PlatformQueryResult?{

        //Link Schema: https://gaana.com/type/link
        val gaanaLink = fullLink.substringAfter("gaana.com/")

        val link = gaanaLink.substringAfterLast('/', "error")
        val type = gaanaLink.substringBeforeLast('/', "error").substringAfterLast('/')

        log("Gaana Fragment", "$type : $link")

        //Error
        if (type == "Error" || link == "Error"){
            showDialog("Please Check Your Link!")
            return null
        }
        return gaanaSearch(
            type,
            link
        )
    }

    private suspend fun gaanaSearch(
        type:String,
        link:String,
    ): PlatformQueryResult {
        val result = PlatformQueryResult(
            folderType = "",
            subFolder = link,
            title = link,
            coverUrl = gaanaPlaceholderImageUrl,
            trackList = listOf(),
            Source.Gaana
        )
        with(result) {
            when (type) {
                "song" -> {
                    gaanaInterface.getGaanaSong(seokey = link).value?.tracks?.firstOrNull()?.also {
                        folderType = "Tracks"
                        subFolder = ""
                        if (File(
                                finalOutputDir(
                                    it.track_title,
                                    folderType,
                                    subFolder,
                                    defaultDir
                                )
                            ).exists()
                        ) {//Download Already Present!!
                            it.downloaded = DownloadStatus.Downloaded
                        }
                        trackList = listOf(it).toTrackDetailsList(folderType, subFolder)
                        title = it.track_title
                        coverUrl = it.artworkLink
                        withContext(Dispatchers.IO) {
                            databaseDAO.insert(
                                DownloadRecord(
                                    type = "Track",
                                    name = title,
                                    link = "https://gaana.com/$type/$link",
                                    coverUrl = coverUrl,
                                    totalFiles = 1,
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
                                        subFolder,
                                        defaultDir
                                    )
                                ).exists()
                            ) {//Download Already Present!!
                                track.downloaded = DownloadStatus.Downloaded
                            }
                        }
                        trackList = it.tracks.toTrackDetailsList(folderType, subFolder)
                        title = link
                        coverUrl = it.custom_artworks.size_480p
                        withContext(Dispatchers.IO) {
                            databaseDAO.insert(
                                DownloadRecord(
                                    type = "Album",
                                    name = title,
                                    link = "https://gaana.com/$type/$link",
                                    coverUrl = coverUrl,
                                    totalFiles = trackList.size,
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
                                        subFolder,
                                        defaultDir
                                    )
                                ).exists()
                            ) {//Download Already Present!!
                                track.downloaded = DownloadStatus.Downloaded
                            }
                        }
                        trackList = it.tracks.toTrackDetailsList(folderType, subFolder)
                        title = link
                        //coverUrl.value = "TODO"
                        coverUrl = gaanaPlaceholderImageUrl
                        withContext(Dispatchers.IO) {
                            databaseDAO.insert(
                                DownloadRecord(
                                    type = "Playlist",
                                    name = title,
                                    link = "https://gaana.com/$type/$link",
                                    coverUrl = coverUrl,
                                    totalFiles = it.tracks.size,
                                )
                            )
                        }
                    }
                }
                "artist" -> {
                    folderType = "Artist"
                    subFolder = link
                    coverUrl = gaanaPlaceholderImageUrl
                    val artistDetails =
                        gaanaInterface.getGaanaArtistDetails(seokey = link).value?.artist?.firstOrNull()
                            ?.also {
                                title = it.name
                                coverUrl = it.artworkLink ?: gaanaPlaceholderImageUrl
                            }
                    gaanaInterface.getGaanaArtistTracks(seokey = link).value?.also {
                        it.tracks.forEach { track ->
                            if (File(
                                    finalOutputDir(
                                        track.track_title,
                                        folderType,
                                        subFolder,
                                        defaultDir
                                    )
                                ).exists()
                            ) {//Download Already Present!!
                                track.downloaded = DownloadStatus.Downloaded
                            }
                        }
                        trackList = it.tracks.toTrackDetailsList(folderType, subFolder)
                        withContext(Dispatchers.IO) {
                            databaseDAO.insert(
                                DownloadRecord(
                                    type = "Artist",
                                    name = artistDetails?.name ?: link,
                                    link = "https://gaana.com/$type/$link",
                                    coverUrl = coverUrl,
                                    totalFiles = trackList.size,
                                )
                            )
                        }
                    }
                }
                else -> {//TODO Handle Error}
                }
            }
            queryActiveTracks(ctx)
            return result
        }
    }

    private fun List<GaanaTrack>.toTrackDetailsList(type:String , subFolder:String) = this.map {
        TrackDetails(
            title = it.track_title,
            artists = it.artist.map { artist -> artist?.name.toString() },
            durationSec = it.duration,
            albumArt = File(
                imageDir + (it.artworkLink.substringBeforeLast('/').substringAfterLast('/')) + ".jpeg"),
            albumName = it.album_title,
            year = it.release_date,
            comment = "Genres:${it.genre?.map { genre -> genre?.name }?.reduceOrNull { acc, s -> acc + s  }}",
            trackUrl = it.lyrics_url,
            downloaded = it.downloaded ?: DownloadStatus.NotDownloaded,
            source = Source.Gaana,
            albumArtURL = it.artworkLink,
            outputFile = finalOutputDir(it.track_title,type, subFolder,defaultDir,".m4a")
        )
    }
}
