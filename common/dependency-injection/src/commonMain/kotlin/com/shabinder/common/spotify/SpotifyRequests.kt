package com.shabinder.common.spotify

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*

private const val BASE_URL = "https://api.spotify.com/v1/"

private val spotifyRequestsClient by lazy {
    HttpClient {
        install(JsonFeature) {
            serializer = kotlinxSerializer
        }
    }
}

interface SpotifyRequests {

    suspend fun getPlaylist(playlistID: String):Playlist{
        return spotifyRequestsClient.get("$BASE_URL/playlists/$playlistID")
    }

    suspend fun getPlaylistTracks(
        playlistID: String?,
        offset: Int = 0,
        limit: Int = 100
    ):PagingObjectPlaylistTrack{
        return spotifyRequestsClient.get("$BASE_URL/playlists/$playlistID/tracks?offset=$offset&limit=$limit")
    }

    suspend fun getTrack(id: String?):Track{
        return spotifyRequestsClient.get("$BASE_URL/tracks/$id")
    }

    suspend fun getEpisode(id: String?) :Track{
        return spotifyRequestsClient.get("$BASE_URL/episodes/$id")
    }

    suspend fun getShow(id: String?): Track{
        return spotifyRequestsClient.get("$BASE_URL/shows/$id")
    }

    suspend fun getAlbum(id: String):Album{
        return spotifyRequestsClient.get("$BASE_URL/albums/$id")
    }

}