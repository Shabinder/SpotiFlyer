package com.shabinder.common.providers.youtube_to_mp3.requests

import co.touchlab.kermit.Kermit
import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.corsApi
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import com.shabinder.common.models.event.coroutines.map
import io.ktor.client.*

class YoutubeMp3(
    override val httpClient: HttpClient,
    override val logger: Kermit
) : Yt1sMp3 {
    suspend fun getMp3DownloadLink(videoID: String, quality: AudioQuality): SuspendableEvent<String, Throwable> =
        getLinkFromYt1sMp3(videoID, quality).map {
            corsApi + it
        }
}