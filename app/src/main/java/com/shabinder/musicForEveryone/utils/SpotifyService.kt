package com.shabinder.musicForEveryone.utils

import com.shabinder.musicForEveryone.models.*
import retrofit2.http.*


interface SpotifyService {

    @GET("playlists/{playlist_id}")
    suspend fun getPlaylist(@Path("playlist_id") playlistId: String?): Playlist?

    @GET("tracks/{id}")
    suspend fun getTrack(@Path("id") var1: String?): Track?

     @GET("episodes/{id}")
    suspend fun getEpisode(@Path("id") var1: String?): Track?

     @GET("shows/{id}")
    suspend fun getShow(@Path("id") var1: String?): Track?

    @GET("albums/{id}")
    suspend fun getAlbum(@Path("id") var1: String?): Album?

    @GET("me")
    suspend fun getMe(): UserPrivate?

}

interface SpotifyServiceToken{

    @POST("api/token")
    @FormUrlEncoded
    suspend fun getToken(@Field("grant_type") grant_type:String = "client_credentials"):Token?

}
