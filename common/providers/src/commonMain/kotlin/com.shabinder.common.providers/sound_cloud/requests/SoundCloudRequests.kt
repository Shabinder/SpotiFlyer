package com.shabinder.common.providers.sound_cloud.requests

import com.shabinder.common.models.SpotiFlyerException
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.utils.requireNotNull
import io.github.shabinder.utils.getBoolean
import io.github.shabinder.utils.getString
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import kotlinx.serialization.json.JsonObject

interface SoundCloudRequests {

    val httpClient: HttpClient


    suspend fun parseURL(url: String) {
        getItem(url).let { item: JsonObject ->
            when (item.getString("kind")) {
                "track" -> {

                }
                "playlist" -> {

                }
                "user" -> {

                }
            }
        }
    }

    @Suppress("NAME_SHADOWING")
    suspend fun getTrack(track: JsonObject): TrackDetails? {
        val track = getTrackInfo(track)
        val title = track.getString("title")

        if (track.getString("policy") == "BLOCK")
            throw SpotiFlyerException.GeoLocationBlocked(extraInfo = "Use VPN to access $title")

        if (track.getBoolean("streamable") == false)
            throw SpotiFlyerException.LinkInvalid("\nSound Cloud Reports that $title is not streamable !\n")

        return null
    }


    suspend fun getTrackInfo(track: JsonObject): JsonObject {
        if (track.containsKey("media"))
            return track

        val infoURL = URLS.TRACK_INFO.buildURL(track.getString("id").requireNotNull())
        return httpClient.get(infoURL) {
            parameter("client_id", CLIENT_ID)
        }
    }


    suspend fun getItem(url: String, clientID: String = CLIENT_ID): JsonObject {
        val itemURL = URLS.RESOLVE.buildURL(url)
        val resp: JsonObject = try {
            httpClient.get(itemURL) {
                parameter("client_id", clientID)
            }
        } catch (e: ClientRequestException) {
            if (clientID != ALT_CLIENT_ID)
                return getItem(url, ALT_CLIENT_ID)
            throw e
        }
        val tracksPresent = resp.getString("kind").equals("playlist") && resp.containsKey("tracks")

        if (!tracksPresent && clientID != ALT_CLIENT_ID)
            return getItem(ALT_CLIENT_ID)

        return resp
    }

    companion object {
        private enum class URLS(val buildURL: (arg: String) -> String) {
            RESOLVE({ "https://api-v2.soundcloud.com/resolve?url=$it}" }),
            PLAYLIST_LIKED({ "https://api-v2.soundcloud.com/users/$it/playlists/liked_and_owned?limit=200" }),
            FAVORITES({ "'https://api-v2.soundcloud.com/users/$it/track_likes?limit=200" }),
            COMMENTED({ "https://api-v2.soundcloud.com/users/$it/comments" }),
            TRACKS({ "https://api-v2.soundcloud.com/users/$it/tracks?limit=200" }),
            ALL({ "https://api-v2.soundcloud.com/profile/soundcloud:users:$it?limit=200" }),
            TRACK_INFO({ "https://api-v2.soundcloud.com/tracks/$it" }),
            ORIGINAL_DOWNLOAD({ "https://api-v2.soundcloud.com/tracks/$it/download" }),
            USER({ "https://api-v2.soundcloud.com/users/$it" }),
            ME({ "https://api-v2.soundcloud.com/me?oauth_token=$it" }),
        }

        private const val CLIENT_ID = "a3e059563d7fd3372b49b37f00a00bcf"
        private const val ALT_CLIENT_ID = "2t9loNQH90kzJcsFCODdigxfp325aq4z"
    }
}