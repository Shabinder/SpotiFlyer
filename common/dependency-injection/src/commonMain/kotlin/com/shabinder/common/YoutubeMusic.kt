package com.shabinder.common

import co.touchlab.kermit.Logger
import io.ktor.client.*

expect class YoutubeMusic(
    logger: Logger,
    httpClient: HttpClient
) {
    fun getYTTracks(response: String): List<YoutubeTrack>
    fun sortByBestMatch(
        ytTracks: List<YoutubeTrack>,
        trackName: String,
        trackArtists: List<String>,
        trackDurationSec: Int
    ): Map<String, Int>

    suspend fun getYoutubeMusicResponse(query: String): String

}