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
import com.shabinder.spotiflyer.models.gaana.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

const val gaana_token = "b2e6d7fbc136547a940516e9b77e5990"

interface GaanaInterface {

    /*
    * Api Request:  http://api.gaana.com/?type=playlist&subtype=playlist_detail&seokey=gaana-dj-hindi-top-50-1&token=b2e6d7fbc136547a940516e9b77e5990&format=JSON
    *
    * subtype : ["most_popular_playlist" , "playlist_home_featured" ,"playlist_detail" ,"user_playlist" ,"topCharts"]
    **/
    @GET(".")
    suspend fun getGaanaPlaylist(
        @Query("type") type: String = "playlist",
        @Query("subtype") subtype: String = "playlist_detail",
        @Query("seokey") seokey: String,
        @Query("token") token: String = gaana_token,
        @Query("format") format: String = "JSON",
        @Query("limit") limit: Int = 2000
    ): Optional<GaanaPlaylist>

    /*
    * Api Request:  http://api.gaana.com/?type=album&subtype=album_detail&seokey=kabir-singh&token=b2e6d7fbc136547a940516e9b77e5990&format=JSON
    *
    * subtype : ["most_popular" , "new_release" ,"featured_album" ,"similar_album" ,"all_albums", "album" ,"album_detail" ,"album_detail_info"]
    **/
    @GET(".")
    suspend fun getGaanaAlbum(
        @Query("type") type: String = "album",
        @Query("subtype") subtype: String = "album_detail",
        @Query("seokey") seokey: String,
        @Query("token") token: String = gaana_token,
        @Query("format") format: String = "JSON",
        @Query("limit") limit: Int = 2000
    ): Optional<GaanaAlbum>

    /*
    * Api Request:  http://api.gaana.com/?type=song&subtype=song_detail&seokey=pachtaoge&token=b2e6d7fbc136547a940516e9b77e5990&format=JSON
    *
    * subtype : ["most_popular" , "hot_songs" ,"recommendation" ,"song_detail"]
    **/
    @GET(".")
    suspend fun getGaanaSong(
        @Query("type") type: String = "song",
        @Query("subtype") subtype: String = "song_detail",
        @Query("seokey") seokey: String,
        @Query("token") token: String = gaana_token,
        @Query("format") format: String = "JSON",
    ): Optional<GaanaSong>

    /*
    * Api Request:  https://api.gaana.com/?type=artist&subtype=artist_details_info&seokey=neha-kakkar&token=b2e6d7fbc136547a940516e9b77e5990&format=JSON
    *
    * subtype : ["most_popular" , "artist_list" ,"artist_track_listing" ,"artist_album" ,"similar_artist","artist_details" ,"artist_details_info"]
    **/
    @GET(".")
    suspend fun getGaanaArtistDetails(
        @Query("type") type: String = "artist",
        @Query("subtype") subtype: String = "artist_details_info",
        @Query("seokey") seokey: String,
        @Query("token") token: String = gaana_token,
        @Query("format") format: String = "JSON",
    ): Optional<GaanaArtistDetails>
    /*
    * Api Request:  http://api.gaana.com/?type=artist&subtype=artist_track_listing&seokey=neha-kakkar&limit=50&token=b2e6d7fbc136547a940516e9b77e5990&format=JSON
    *
    * subtype : ["most_popular" , "artist_list" ,"artist_track_listing" ,"artist_album" ,"similar_artist","artist_details" ,"artist_details_info"]
    **/
    @GET(".")
    suspend fun getGaanaArtistTracks(
        @Query("type") type: String = "artist",
        @Query("subtype") subtype: String = "artist_track_listing",
        @Query("seokey") seokey: String,
        @Query("token") token: String = gaana_token,
        @Query("format") format: String = "JSON",
        @Query("limit") limit: Int = 50
    ): Optional<GaanaArtistTracks>

    /*
    * Dynamic Url Requests
    * */
    @GET
    fun getResponse(@Url url:String): Call<ResponseBody>
}