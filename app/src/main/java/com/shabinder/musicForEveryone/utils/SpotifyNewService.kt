package com.shabinder.musicForEveryone.utils

import kaaes.spotify.webapi.android.models.*
import retrofit2.http.GET
import retrofit2.http.Path


interface SpotifyNewService {

    @GET("playlists/{playlist_id}")
    suspend fun getPlaylist(@Path("playlist_id") playlistId: String?): Playlist?

    @GET("tracks/{id}")
    suspend fun getTrack(@Path("id") var1: String?): Track?

    @GET("albums/{id}")
    suspend fun getAlbum(@Path("id") var1: String?): Album?

    @GET("me")
    suspend fun getMe(): com.shabinder.musicForEveryone.utils.UserPrivate?


}

data class UserPrivate(
    val country:String,
    var display_name: String,
    val email:String,
    var external_urls: Map<String?, String?>? = null,
    var followers: Followers? = null,
    var href: String? = null,
    var id: String? = null,
    var images: List<Image?>? = null,
    var product:String,
    var type: String? = null,
    var uri: String? = null)