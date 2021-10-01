package com.shabinder.common.providers.sound_cloud.requests

import com.shabinder.common.models.SpotiFlyerException
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.soundcloud.resolvemodel.SoundCloudResolveResponseBase
import com.shabinder.common.models.soundcloud.resolvemodel.SoundCloudResolveResponseBase.SoundCloudResolveResponsePlaylist
import com.shabinder.common.models.soundcloud.resolvemodel.SoundCloudResolveResponseBase.SoundCloudResolveResponseTrack
import io.github.shabinder.utils.getBoolean
import io.github.shabinder.utils.getString
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.JsonObject

interface SoundCloudRequests {

    val httpClient: HttpClient


    suspend fun parseURL(url: String) {
        getResponseObj(url).let { item ->
            when (item) {
                is SoundCloudResolveResponseTrack -> {
                    getTrack(item)
                }
                is SoundCloudResolveResponsePlaylist -> {
                    
                }
                else -> throw SpotiFlyerException.FeatureNotImplementedYet()
            }
        }
    }

    @Suppress("NAME_SHADOWING")
    suspend fun getTrack(track: SoundCloudResolveResponseTrack): TrackDetails? {
        val track = getTrackInfo(track)

        if (track.policy == "BLOCK")
            throw SpotiFlyerException.GeoLocationBlocked(extraInfo = "Use VPN to access ${track.title}")

        if (!track.streamable)
            throw SpotiFlyerException.LinkInvalid("\nSound Cloud Reports that ${track.title} is not streamable !\n")

        return null
    }


    suspend fun getTrackInfo(res: SoundCloudResolveResponseTrack): SoundCloudResolveResponseTrack {
        if (res.media.transcodings.isNotEmpty())
            return res

        val infoURL = URLS.TRACK_INFO.buildURL(res.id.toString())
        return httpClient.get(infoURL) {
            parameter("client_id", CLIENT_ID)
        }
    }

    suspend fun getResponseObj(url: String, clientID: String = CLIENT_ID): SoundCloudResolveResponseBase {
        val itemURL = URLS.RESOLVE.buildURL(url)
        val resp: SoundCloudResolveResponseBase = try {
            httpClient.get(itemURL) {
                parameter("client_id", clientID)
            }
        } catch (e: ClientRequestException) {
            if (clientID != ALT_CLIENT_ID)
                return getResponseObj(url, ALT_CLIENT_ID)
            throw e
        }
        val tracksPresent = (resp is SoundCloudResolveResponsePlaylist && resp.tracks.isNotEmpty())

        if (!tracksPresent && clientID != ALT_CLIENT_ID)
            return getResponseObj(ALT_CLIENT_ID)

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

        const val CLIENT_ID = "a3e059563d7fd3372b49b37f00a00bcf"
        const val ALT_CLIENT_ID = "2t9loNQH90kzJcsFCODdigxfp325aq4z"
    }
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> SoundCloudRequests.doAuthenticatedRequest(url: String): T {
    var clientID: String = SoundCloudRequests.CLIENT_ID
    return try {
        httpClient.get(url) {
            parameter("client_id", clientID)
        }
    } catch (e: ClientRequestException) {
        if (clientID != SoundCloudRequests.ALT_CLIENT_ID) {
            clientID = SoundCloudRequests.ALT_CLIENT_ID
            return httpClient.get(url) {
                parameter("client_id", clientID)
            }
        }
        throw e
    }
}