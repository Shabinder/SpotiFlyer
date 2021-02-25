/*
 * Copyright (c)  2021  Shabinder Singh
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
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.common.di.providers

import co.touchlab.kermit.Kermit
import com.shabinder.common.database.DownloadRecordDatabaseQueries
import com.shabinder.common.di.*
import com.shabinder.common.di.spotify.SpotifyRequests
import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.spotify.Album
import com.shabinder.common.models.spotify.Image
import com.shabinder.common.models.spotify.Source
import com.shabinder.common.models.spotify.Track
import com.shabinder.database.Database
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SpotifyProvider(
    private val tokenStore: TokenStore,
    private val database: Database,
    private val logger: Kermit,
    private val dir: Dir,
) : SpotifyRequests {

    init {
        logger.d { "Creating Spotify Provider" }
        GlobalScope.launch(Dispatchers.Default) {authenticateSpotify()}
    }

    override suspend fun authenticateSpotify(): HttpClient?{
        val token = tokenStore.getToken()
        return if(token == null) {
            showPopUpMessage("Please Check your Network Connection")
            null
        }
        else{
            logger.d { "Spotify Provider Created with $token" }
            httpClient = HttpClient {
                defaultRequest {
                    header("Authorization","Bearer ${token.access_token}")
                }
                install(JsonFeature) {
                    serializer = kotlinxSerializer
                }
            }
            httpClient
        }
    }

    override lateinit var httpClient: HttpClient

    private val db:DownloadRecordDatabaseQueries
        get() = database.downloadRecordDatabaseQueries

    suspend fun query(fullLink: String): PlatformQueryResult?{

        if(!this::httpClient.isInitialized){
            authenticateSpotify()
        }

        var spotifyLink =
            "https://" + fullLink.substringAfterLast("https://").substringBefore(" ").trim()

        if (!spotifyLink.contains("open.spotify")) {
            //Very Rare instance
            spotifyLink = resolveLink(spotifyLink)
        }

        val link = spotifyLink.substringAfterLast('/', "Error").substringBefore('?')
        val type = spotifyLink.substringBeforeLast('/', "Error").substringAfterLast('/')


        if (type == "Error" || link == "Error") {
            return null
        }

        if (type == "episode" || type == "show") {
            //TODO Implementation
            return null
        }

        return spotifySearch(
            type,
            link
        )
    }

    private suspend fun spotifySearch(
        type:String,
        link: String
    ): PlatformQueryResult {
        val result = PlatformQueryResult(
            folderType = "",
            subFolder = "",
            title = "",
            coverUrl = "",
            trackList = listOf(),
            Source.Spotify
        )
        with(result) {
            when (type) {
                "track" -> {
                    getTrack(link).also {
                        folderType = "Tracks"
                        subFolder = ""
                        if (dir.isPresent(
                                dir.finalOutputDir(
                                    it.name.toString(),
                                    folderType,
                                    subFolder,
                                    dir.defaultDir()
                                )
                            )
                        ) {//Download Already Present!!
                            it.downloaded = com.shabinder.common.models.DownloadStatus.Downloaded
                        }
                        trackList = listOf(it).toTrackDetailsList(folderType, subFolder)
                        title = it.name.toString()
                        coverUrl = (it.album?.images?.elementAtOrNull(1)?.url
                            ?: it.album?.images?.elementAtOrNull(0)?.url).toString()
                        withContext(Dispatchers.Default) {
                            db.add(
                                    type = "Track",
                                    name = title,
                                    link = "https://open.spotify.com/$type/$link",
                                    coverUrl = coverUrl,
                                    totalFiles = 1,
                            )
                        }
                    }
                }

                "album" -> {
                    val albumObject = getAlbum(link)
                    folderType = "Albums"
                    subFolder = albumObject.name.toString()
                    albumObject.tracks?.items?.forEach {
                        if (dir.isPresent(
                                dir.finalOutputDir(
                                    it.name.toString(),
                                    folderType,
                                    subFolder,
                                    dir.defaultDir()
                                )
                            )
                        ) {//Download Already Present!!
                            it.downloaded = com.shabinder.common.models.DownloadStatus.Downloaded
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
                    albumObject.tracks?.items?.toTrackDetailsList(folderType, subFolder).let {
                        if (it.isNullOrEmpty()) {
                            //TODO Handle Error
                        } else {
                            trackList = it
                            title = albumObject.name.toString()
                            coverUrl = (albumObject.images?.elementAtOrNull(1)?.url
                                ?: albumObject.images?.elementAtOrNull(0)?.url).toString()
                            withContext(Dispatchers.Default) {
                                db.add(
                                    type = "Album",
                                    name = title,
                                    link = "https://open.spotify.com/$type/$link",
                                    coverUrl = coverUrl,
                                    totalFiles = trackList.size.toLong(),
                                )
                            }
                        }
                    }
                }

                "playlist" -> {
                    val playlistObject = getPlaylist(link)
                    folderType = "Playlists"
                    subFolder = playlistObject.name.toString()
                    val tempTrackList = mutableListOf<Track>()
                    //log("Tracks Fetched", playlistObject.tracks?.items?.size.toString())
                    playlistObject.tracks?.items?.forEach {
                        it.track?.let { it1 ->
                            if (dir.isPresent(
                                    dir.finalOutputDir(
                                        it1.name.toString(),
                                        folderType,
                                        subFolder,
                                        dir.defaultDir()
                                    )
                                )
                            ) {//Download Already Present!!
                                it1.downloaded = com.shabinder.common.models.DownloadStatus.Downloaded
                            }
                            tempTrackList.add(it1)
                        }
                    }
                    var moreTracksAvailable = !playlistObject.tracks?.next.isNullOrBlank()

                    while (moreTracksAvailable) {
                        //Check For More Tracks If available
                        val moreTracks =
                            getPlaylistTracks(link, offset = tempTrackList.size)
                        moreTracks.items?.forEach {
                            it.track?.let { it1 -> tempTrackList.add(it1) }
                        }
                        moreTracksAvailable = !moreTracks.next.isNullOrBlank()
                    }
                    //log("Total Tracks Fetched", tempTrackList.size.toString())
                    trackList = tempTrackList.toTrackDetailsList(folderType, subFolder)
                    title = playlistObject.name.toString()
                    coverUrl = playlistObject.images?.elementAtOrNull(1)?.url
                        ?: playlistObject.images?.firstOrNull()?.url.toString()
                    withContext(Dispatchers.Default) {
                        db.add(
                            type = "Playlist",
                            name = title,
                            link = "https://open.spotify.com/$type/$link",
                            coverUrl = coverUrl,
                            totalFiles = tempTrackList.size.toLong(),
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
        return result
    }

    /*
    * New Link Schema: https://link.tospotify.com/kqTBblrjQbb,
    * Fetching Standard Link: https://open.spotify.com/playlist/37i9dQZF1DX9RwfGbeGQwP?si=iWz7B1tETiunDntnDo3lSQ&amp;_branch_match_id=862039436205270630
    * */
    private suspend fun resolveLink(
        url:String
    ):String {
        val response = getResponse(url)
        val regex = """https://open\.spotify\.com.+\w""".toRegex()
        return regex.find(response)?.value.toString()
    }

    private fun List<Track>.toTrackDetailsList(type:String, subFolder:String) = this.map {
        TrackDetails(
            title = it.name.toString(),
            artists = it.artists?.map { artist -> artist?.name.toString() } ?: listOf(),
            durationSec = (it.duration_ms/1000).toInt(),
            albumArtPath = dir.imageCacheDir() + (it.album?.images?.elementAtOrNull(1)?.url ?: it.album?.images?.firstOrNull()?.url.toString()).substringAfterLast('/') + ".jpeg",
            albumName = it.album?.name,
            year = it.album?.release_date,
            comment = "Genres:${it.album?.genres?.joinToString()}",
            trackUrl = it.href,
            downloaded = it.downloaded,
            source = Source.Spotify,
            albumArtURL = it.album?.images?.elementAtOrNull(1)?.url ?: it.album?.images?.firstOrNull()?.url.toString(),
            outputFilePath = dir.finalOutputDir(it.name.toString(),type, subFolder,dir.defaultDir()/*,".m4a"*/)
        )
    }
}