package com.shabinder.common.providers.sound_cloud.requests

import com.shabinder.common.core_components.utils.getFinalUrl
import com.shabinder.common.models.SpotiFlyerException
import com.shabinder.common.models.soundcloud.resolvemodel.SoundCloudResolveResponseBase
import com.shabinder.common.models.soundcloud.resolvemodel.SoundCloudResolveResponseBase.SoundCloudResolveResponsePlaylist
import com.shabinder.common.models.soundcloud.resolvemodel.SoundCloudResolveResponseBase.SoundCloudResolveResponseTrack
import com.shabinder.common.utils.globalJson
import io.ktor.client.HttpClient
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface SoundCloudRequests {

    val httpClient: HttpClient

    suspend fun fetchResult(url: String): SoundCloudResolveResponseBase {
        @Suppress("NAME_SHADOWING")
        var url = url

        // Fetch Full URL if Input is Shortened URL from App
        if (url.contains("soundcloud.app"))
            url = httpClient.getFinalUrl(url)

        return getResponseObj(url).run {
            when (this) {
                is SoundCloudResolveResponseTrack -> {
                    getTrack()
                }
                is SoundCloudResolveResponsePlaylist -> {
                    populatePlaylist()
                }
                else -> throw SpotiFlyerException.FeatureNotImplementedYet()
            }
        }
    }

    @Suppress("NAME_SHADOWING")
    suspend fun SoundCloudResolveResponseTrack.getTrack() = apply {
        val track = populateTrackInfo()

        if (track.policy == "BLOCK")
            throw SpotiFlyerException.GeoLocationBlocked(extraInfo = "Use VPN to access ${track.title}")

        if (!track.streamable)
            throw SpotiFlyerException.LinkInvalid("\nSound Cloud Reports that ${track.title} is not streamable !\n")

        return track
    }

    @Suppress("NAME_SHADOWING")
    suspend fun SoundCloudResolveResponsePlaylist.populatePlaylist(): SoundCloudResolveResponsePlaylist = apply {
        supervisorScope {
            try {
                tracks = tracks.map {
                    async {
                        runCatching {
                            it.populateTrackInfo()
                        }.getOrNull() ?: it
                    }
                }.awaitAll()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }


    private suspend fun SoundCloudResolveResponseTrack.populateTrackInfo(): SoundCloudResolveResponseTrack {
        if (media.transcodings.isNotEmpty())
            return this

        val infoURL = URLS.TRACK_INFO.buildURL(id)

        val data: String = httpClient.get(infoURL) {
            parameter("client_id", CLIENT_ID)
        }
        return globalJson.decodeFromString(data)
    }

    private suspend fun getResponseObj(url: String, clientID: String = CLIENT_ID): SoundCloudResolveResponseBase {
        val itemURL = URLS.RESOLVE.buildURL(url)
        val resp: SoundCloudResolveResponseBase = try {
            val data: String = httpClient.get(itemURL) {
                parameter("client_id", clientID)
            }
            globalJson.decodeFromString(SoundCloudSerializer, data)
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

    @Suppress("unused")
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

        object SoundCloudSerializer :
            JsonContentPolymorphicSerializer<SoundCloudResolveResponseBase>(SoundCloudResolveResponseBase::class) {
            override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out SoundCloudResolveResponseBase> =
                when {
                    "track_count" in element.jsonObject -> SoundCloudResolveResponsePlaylist.serializer()
                    "kind" in element.jsonObject -> {
                        val isTrack =
                            element.jsonObject["kind"]
                                ?.jsonPrimitive?.content.toString()
                                .contains("track", true)
                        when {
                            isTrack || "track_format" in element.jsonObject -> SoundCloudResolveResponseTrack.serializer()
                            else -> SoundCloudResolveResponsePlaylist.serializer()
                        }
                    }
                    else -> SoundCloudResolveResponsePlaylist.serializer()
                }
        }
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