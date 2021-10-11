/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.common.providers.gaana.requests

import com.shabinder.common.models.corsApi
import com.shabinder.common.models.gaana.GaanaAlbum
import com.shabinder.common.models.gaana.GaanaArtistDetails
import com.shabinder.common.models.gaana.GaanaArtistTracks
import com.shabinder.common.models.gaana.GaanaPlaylist
import com.shabinder.common.models.gaana.GaanaSong
import io.ktor.client.*
import io.ktor.client.request.*

interface GaanaRequests {

    companion object {
        private const val TOKEN = "b2e6d7fbc136547a940516e9b77e5990"
        private val BASE_URL get() = "${corsApi}https://api.gaana.com"
    }

    val httpClient: HttpClient

    /*
    * Api Request:  http://api.gaana.com/?type=playlist&subtype=playlist_detail&seokey=gaana-dj-hindi-top-50-1&token=b2e6d7fbc136547a940516e9b77e5990&format=JSON
    *
    * subtype : ["most_popular_playlist" , "playlist_home_featured" ,"playlist_detail" ,"user_playlist" ,"topCharts"]
    **/
    suspend fun getGaanaPlaylist(
        type: String = "playlist",
        subtype: String = "playlist_detail",
        seokey: String,
        format: String = "JSON",
        limit: Int = 2000
    ): GaanaPlaylist {
        return httpClient.get(
            "$BASE_URL/?type=$type&subtype=$subtype&seokey=$seokey&token=$TOKEN&format=$format&limit=$limit"
        )
    }

    /*
    * Api Request:  http://api.gaana.com/?type=album&subtype=album_detail&seokey=kabir-singh&token=b2e6d7fbc136547a940516e9b77e5990&format=JSON
    *
    * subtype : ["most_popular" , "new_release" ,"featured_album" ,"similar_album" ,"all_albums", "album" ,"album_detail" ,"album_detail_info"]
    **/
    suspend fun getGaanaAlbum(
        type: String = "album",
        subtype: String = "album_detail",
        seokey: String,
        format: String = "JSON",
        limit: Int = 2000
    ): GaanaAlbum {
        return httpClient.get(
            "$BASE_URL/?type=$type&subtype=$subtype&seokey=$seokey&token=$TOKEN&format=$format&limit=$limit"
        )
    }

    /*
    * Api Request:  http://api.gaana.com/?type=song&subtype=song_detail&seokey=pachtaoge&token=b2e6d7fbc136547a940516e9b77e5990&format=JSON
    *
    * subtype : ["most_popular" , "hot_songs" ,"recommendation" ,"song_detail"]
    **/
    suspend fun getGaanaSong(
        type: String = "song",
        subtype: String = "song_detail",
        seokey: String,
        format: String = "JSON",
    ): GaanaSong {
        return httpClient.get(
            "$BASE_URL/?type=$type&subtype=$subtype&seokey=$seokey&token=$TOKEN&format=$format"
        )
    }

    /*
    * Api Request:  https://api.gaana.com/?type=artist&subtype=artist_details_info&seokey=neha-kakkar&token=b2e6d7fbc136547a940516e9b77e5990&format=JSON
    *
    * subtype : ["most_popular" , "artist_list" ,"artist_track_listing" ,"artist_album" ,"similar_artist","artist_details" ,"artist_details_info"]
    **/
    suspend fun getGaanaArtistDetails(
        type: String = "artist",
        subtype: String = "artist_details_info",
        seokey: String,
        format: String = "JSON",
    ): GaanaArtistDetails {
        return httpClient.get(
            "$BASE_URL/?type=$type&subtype=$subtype&seokey=$seokey&token=$TOKEN&format=$format"
        )
    }

    /*
    * Api Request:  http://api.gaana.com/?type=artist&subtype=artist_track_listing&seokey=neha-kakkar&limit=50&token=b2e6d7fbc136547a940516e9b77e5990&format=JSON
    *
    * subtype : ["most_popular" , "artist_list" ,"artist_track_listing" ,"artist_album" ,"similar_artist","artist_details" ,"artist_details_info"]
    **/
    suspend fun getGaanaArtistTracks(
        type: String = "artist",
        subtype: String = "artist_track_listing",
        seokey: String,
        format: String = "JSON",
        limit: Int = 50
    ): GaanaArtistTracks {
        return httpClient.get(
            "$BASE_URL/?type=$type&subtype=$subtype&seokey=$seokey&token=$TOKEN&format=$format&limit=$limit"
        )
    }
}
