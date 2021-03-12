package com.shabinder.common.di.spotify

import com.shabinder.common.models.spotify.Album
import com.shabinder.common.models.spotify.PagingObjectPlaylistTrack
import com.shabinder.common.models.spotify.Playlist
import com.shabinder.common.models.spotify.Track
import io.ktor.client.*
import io.ktor.client.request.*

private const val BASE_URL = "https://api.spotify.com/v1"

interface SpotifyRequests {

    val httpClient:HttpClient

    suspend fun authenticateSpotifyClient(override:Boolean = false):HttpClient?

    suspend fun getPlaylist(playlistID: String): Playlist {
        return httpClient.get("$BASE_URL/playlists/$playlistID")
    }

    suspend fun getPlaylistTracks(
        playlistID: String?,
        offset: Int = 0,
        limit: Int = 100
    ): PagingObjectPlaylistTrack {
        return httpClient.get("$BASE_URL/playlists/$playlistID/tracks?offset=$offset&limit=$limit")
    }

    suspend fun getTrack(id: String?): Track {
        return httpClient.get("$BASE_URL/tracks/$id")
    }

    suspend fun getEpisode(id: String?) : Track {
        return httpClient.get("$BASE_URL/episodes/$id")
    }

    suspend fun getShow(id: String?): Track {
        return httpClient.get("$BASE_URL/shows/$id")
    }

    suspend fun getAlbum(id: String): Album {
        return httpClient.get("$BASE_URL/albums/$id")
    }

    suspend fun getResponse(url:String):String{
        return httpClient.get(url)
    }
}