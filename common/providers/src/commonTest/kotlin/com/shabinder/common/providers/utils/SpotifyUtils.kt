package com.shabinder.common.providers.utils

import com.shabinder.common.core_components.file_manager.finalOutputDir
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.NativeAtomicReference
import com.shabinder.common.models.SpotiFlyerException
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.spotify.Source
import com.shabinder.common.models.spotify.Track
import com.shabinder.common.providers.spotify.requests.SpotifyRequests
import com.shabinder.common.providers.spotify.requests.authenticateSpotify
import com.shabinder.common.utils.globalJson
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*

object SpotifyUtils {

    suspend fun getSpotifyRequests(spotifyToken: String? = null): SpotifyRequests {
        val spotifyClient = getSpotifyClient(spotifyToken)
        return object : SpotifyRequests {
            override val httpClientRef: NativeAtomicReference<HttpClient> = NativeAtomicReference(spotifyClient)
            override suspend fun authenticateSpotifyClient(override: Boolean) { httpClientRef.value = getSpotifyClient(spotifyToken) }
        }
    }

    suspend fun getSpotifyClient(spotifyToken: String? = null): HttpClient {
        val token = spotifyToken ?: authenticateSpotify().component1()?.access_token
        return if (token == null) {
            println("Spotify Auth Failed: Please Check your Network Connection")
            throw SpotiFlyerException.NoInternetException()
        } else {
            println("Spotify Token: $token")
            HttpClient {
                defaultRequest {
                    header("Authorization", "Bearer $token")
                }
                install(JsonFeature) {
                    serializer = KotlinxSerializer(globalJson)
                }
            }
        }
    }

    fun Track.toTrackDetailsList(type: String = "Track", subFolder: String = "SpotifyFolder") = let {
        TrackDetails(
            title = it.name.toString(),
            trackNumber = it.track_number,
            genre = it.album?.genres?.filterNotNull() ?: emptyList(),
            artists = it.artists?.map { artist -> artist?.name.toString() } ?: listOf(),
            albumArtists = it.album?.artists?.mapNotNull { artist -> artist?.name } ?: emptyList(),
            durationSec = (it.duration_ms / 1000).toInt(),
            albumArtPath = (it.album?.images?.firstOrNull()?.url.toString()).substringAfterLast(
                '/'
            ) + ".jpeg",
            albumName = it.album?.name,
            year = it.album?.release_date,
            comment = "Genres:${it.album?.genres?.joinToString()}",
            trackUrl = it.href,
            downloaded = DownloadStatus.NotDownloaded,
            source = Source.Spotify,
            albumArtURL = it.album?.images?.firstOrNull()?.url.toString(),
            outputFilePath = ""
        )
    }
}