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

package com.shabinder.spotiflyer.networking

import com.shabinder.spotiflyer.models.Optional
import com.shabinder.spotiflyer.models.spotify.*
import retrofit2.http.*

interface SpotifyService {

    @GET("playlists/{playlist_id}")
    suspend fun getPlaylist(@Path("playlist_id") playlistId: String?): Optional<Playlist>

    @GET("playlists/{playlist_id}/tracks")
    suspend fun getPlaylistTracks(
        @Path("playlist_id") playlistId: String?,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 100
    ): Optional<PagingObjectPlaylistTrack>

    @GET("tracks/{id}")
    suspend fun getTrack(@Path("id") trackId: String?): Optional<Track>

     @GET("episodes/{id}")
    suspend fun getEpisode(@Path("id") episodeId: String?): Optional<Track>

     @GET("shows/{id}")
    suspend fun getShow(@Path("id") showId: String?): Optional<Track>

    @GET("albums/{id}")
    suspend fun getAlbum(@Path("id") albumId: String?): Optional<Album>
}

interface SpotifyServiceTokenRequest{

    @POST("api/token")
    @FormUrlEncoded
    suspend fun getToken(@Field("grant_type") grant_type:String = "client_credentials"): Optional<Token>

}
