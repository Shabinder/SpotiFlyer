package com.shabinder.common.providers.utils

import co.touchlab.kermit.Kermit
import com.shabinder.common.core_components.utils.createHttpClient
import com.shabinder.common.database.getLogger
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.providers.placeholders.FileManagerPlaceholder
import com.shabinder.common.providers.youtube.YoutubeProvider
import com.shabinder.common.providers.youtube_music.YoutubeMusic
import com.shabinder.common.providers.youtube_to_mp3.requests.YoutubeMp3

object CommonUtils {
    val httpClient by lazy { createHttpClient() }
    val logger by lazy { Kermit(getLogger()) }
    val youtubeProvider by lazy { YoutubeProvider(httpClient, logger, FileManagerPlaceholder) }
    val youtubeMp3 = YoutubeMp3(httpClient, logger)
    val youtubeMusic = YoutubeMusic(logger, httpClient, youtubeProvider, youtubeMp3, FileManagerPlaceholder)

    fun getYTQueryString(trackDetails: TrackDetails) = "${trackDetails.title} - ${trackDetails.artists.joinToString(",")}"
}