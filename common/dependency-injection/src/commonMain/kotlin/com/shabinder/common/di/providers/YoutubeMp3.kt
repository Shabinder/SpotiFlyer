package com.shabinder.common.di.providers

import co.touchlab.kermit.Kermit
import com.shabinder.common.di.Dir
import com.shabinder.common.di.youtubeMp3.Yt1sMp3
import com.shabinder.database.Database
import io.ktor.client.*

class YoutubeMp3(
    override val httpClient: HttpClient,
    private val logger: Kermit,
    private val dir: Dir,
):Yt1sMp3 {
    suspend fun getMp3DownloadLink(videoID:String):String? = getLinkFromYt1sMp3(videoID)
}