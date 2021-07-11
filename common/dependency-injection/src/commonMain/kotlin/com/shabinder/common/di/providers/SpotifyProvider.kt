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
import com.shabinder.common.di.Dir
import com.shabinder.common.di.TokenStore
import com.shabinder.common.di.createHttpClient
import com.shabinder.common.di.finalOutputDir
import com.shabinder.common.di.globalJson
import com.shabinder.common.di.providers.requests.spotify.SpotifyRequests
import com.shabinder.common.di.providers.requests.spotify.authenticateSpotify
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.NativeAtomicReference
import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.common.models.SpotiFlyerException
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import com.shabinder.common.models.spotify.PlaylistTrack
import com.shabinder.common.models.spotify.Source
import com.shabinder.common.models.spotify.Track
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*

class SpotifyProvider(
    private val tokenStore: TokenStore,
    private val logger: Kermit,
    private val dir: Dir,
) : SpotifyRequests {

    override suspend fun authenticateSpotifyClient(override: Boolean) {
        val token = if (override) authenticateSpotify().component1() else tokenStore.getToken()
        if (token == null) {
            logger.d { "Spotify Auth Failed: Please Check your Network Connection" }
        } else {
            logger.d { "Spotify Provider Created with $token" }
            HttpClient {
                defaultRequest {
                    header("Authorization", "Bearer ${token.access_token}")
                }
                install(JsonFeature) {
                    serializer = KotlinxSerializer(globalJson)
                }
            }.also { httpClientRef.value = it }
        }
    }

    override val httpClientRef = NativeAtomicReference(createHttpClient(true))

    suspend fun query(fullLink: String): SuspendableEvent<PlatformQueryResult, Throwable> = SuspendableEvent {

        var spotifyLink =
            "https://" + fullLink.substringAfterLast("https://").substringBefore(" ").trim()

        if (!spotifyLink.contains("open.spotify")) {
            // Very Rare instance
            spotifyLink = resolveLink(spotifyLink)
        }

        val link = spotifyLink.substringAfterLast('/', "Error").substringBefore('?')
        val type = spotifyLink.substringBeforeLast('/', "Error").substringAfterLast('/')

        if (type == "Error" || link == "Error") {
            throw SpotiFlyerException.LinkInvalid(fullLink)
        }

        if (type == "episode" || type == "show") {
            throw SpotiFlyerException.FeatureNotImplementedYet(
                "Support for Spotify's ${type.uppercase()} isn't implemented yet"
            )
        }

        try {
            spotifySearch(
                type,
                link
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Try Reinitialising Client // Handle 401 Token Expiry ,etc Exceptions
            authenticateSpotifyClient(true)

            spotifySearch(
                type,
                link
            )
        }
    }

    private suspend fun spotifySearch(
        type: String,
        link: String
    ): PlatformQueryResult {
        return PlatformQueryResult(
            folderType = "",
            subFolder = "",
            title = "",
            coverUrl = "",
            trackList = listOf(),
            Source.Spotify
        ).apply {
            when (type) {
                "track" -> {
                    getTrack(link).also {
                        folderType = "Tracks"
                        subFolder = ""
                        trackList = listOf(it).toTrackDetailsList(folderType, subFolder)
                        title = it.name.toString()
                        coverUrl = it.album?.images?.elementAtOrNull(0)?.url.toString()
                    }
                }

                "album" -> {
                    val albumObject = getAlbum(link)
                    folderType = "Albums"
                    subFolder = albumObject.name.toString()
                    albumObject.tracks?.items?.forEach { it.album = albumObject }

                    albumObject.tracks?.items?.toTrackDetailsList(folderType, subFolder).let {
                        if (it.isNullOrEmpty()) {
                            // TODO Handle Error
                        } else {
                            trackList = it
                            title = albumObject.name.toString()
                            coverUrl = albumObject.images?.elementAtOrNull(0)?.url.toString()
                        }
                    }
                }

                "playlist" -> {
                    val playlistObject = getPlaylist(link)
                    folderType = "Playlists"
                    subFolder = playlistObject.name.toString()
                    val tempTrackList = mutableListOf<Track>().apply {
                        // Add Fetched Tracks
                        playlistObject.tracks?.items?.mapNotNull(PlaylistTrack::track)?.let {
                            addAll(it)
                        }
                    }

                    // Check For More Tracks If available
                    var moreTracksAvailable = !playlistObject.tracks?.next.isNullOrBlank()
                    while (moreTracksAvailable) {
                        // Fetch Remaining Tracks
                        val moreTracks =
                            getPlaylistTracks(link, offset = tempTrackList.size)
                        moreTracks.items?.mapNotNull(PlaylistTrack::track)?.let { remTracks ->
                            tempTrackList.addAll(remTracks)
                        }
                        moreTracksAvailable = !moreTracks.next.isNullOrBlank()
                    }

                    // log("Total Tracks Fetched", tempTrackList.size.toString())
                    trackList = tempTrackList.toTrackDetailsList(folderType, subFolder)
                    title = playlistObject.name.toString()
                    coverUrl = playlistObject.images?.firstOrNull()?.url.toString()
                }
                "episode" -> { // TODO
                    throw SpotiFlyerException.FeatureNotImplementedYet()
                }
                "show" -> { // TODO
                    throw SpotiFlyerException.FeatureNotImplementedYet()
                }
                else -> {
                    throw SpotiFlyerException.LinkInvalid("Provide: Spotify, Type:$type -> Link:$link")
                }
            }
        }
    }

    /*
    * New Link Schema: https://link.tospotify.com/kqTBblrjQbb,
    * Fetching Standard Link: https://open.spotify.com/playlist/37i9dQZF1DX9RwfGbeGQwP?si=iWz7B1tETiunDntnDo3lSQ&amp;_branch_match_id=862039436205270630
    * */
    private suspend fun resolveLink(
        url: String
    ): String {
        val response = getResponse(url)
        val regex = """https://open\.spotify\.com.+\w""".toRegex()
        return regex.find(response)?.value.toString()
    }

    private fun List<Track>.toTrackDetailsList(type: String, subFolder: String) = this.map {
        TrackDetails(
            title = it.name.toString(),
            trackNumber = it.track_number,
            genre = it.album?.genres?.filterNotNull() ?: emptyList(),
            artists = it.artists?.map { artist -> artist?.name.toString() } ?: listOf(),
            albumArtists = it.album?.artists?.mapNotNull { artist -> artist?.name } ?: emptyList(),
            durationSec = (it.duration_ms / 1000).toInt(),
            albumArtPath = dir.imageCacheDir() + (it.album?.images?.firstOrNull()?.url.toString()).substringAfterLast('/') + ".jpeg",
            albumName = it.album?.name,
            year = it.album?.release_date,
            comment = "Genres:${it.album?.genres?.joinToString()}",
            trackUrl = it.href,
            downloaded = it.updateStatusIfPresent(type, subFolder),
            source = Source.Spotify,
            albumArtURL = it.album?.images?.firstOrNull()?.url.toString(),
            outputFilePath = dir.finalOutputDir(it.name.toString(), type, subFolder, dir.defaultDir()/*,".m4a"*/)
        )
    }
    private fun Track.updateStatusIfPresent(folderType: String, subFolder: String): DownloadStatus {
        return if (dir.isPresent(
                dir.finalOutputDir(
                        name.toString(),
                        folderType,
                        subFolder,
                        dir.defaultDir()
                    )
            )
        ) { // Download Already Present!!
            DownloadStatus.Downloaded.also {
                downloaded = it
            }
        } else downloaded
    }
}
