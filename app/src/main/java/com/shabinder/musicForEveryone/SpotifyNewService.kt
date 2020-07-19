package com.shabinder.musicForEveryone

import kaaes.spotify.webapi.android.models.Playlist
import retrofit.http.GET
import retrofit.http.Path


interface SpotifyNewService {

    @GET("/playlists/{playlist_id}")
    fun getPlaylist(@Path("playlist_id") playlistId: String?): Playlist?



}