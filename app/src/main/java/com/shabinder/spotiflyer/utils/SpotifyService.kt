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

package com.shabinder.spotiflyer.utils

import com.shabinder.spotiflyer.models.*
import retrofit2.http.*

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


interface SpotifyService {

    @GET("playlists/{playlist_id}")
    suspend fun getPlaylist(@Path("playlist_id") playlistId: String?): Playlist

    @GET("tracks/{id}")
    suspend fun getTrack(@Path("id") trackId: String?): Track

     @GET("episodes/{id}")
    suspend fun getEpisode(@Path("id") episodeId: String?): Track

     @GET("shows/{id}")
    suspend fun getShow(@Path("id") showId: String?): Track

    @GET("albums/{id}")
    suspend fun getAlbum(@Path("id") albumId: String?): Album

    @GET("me")
    suspend fun getMe(): UserPrivate?

}

interface SpotifyServiceToken{

    @POST("api/token")
    @FormUrlEncoded
    suspend fun getToken(@Field("grant_type") grant_type:String = "client_credentials"):Token?

}
