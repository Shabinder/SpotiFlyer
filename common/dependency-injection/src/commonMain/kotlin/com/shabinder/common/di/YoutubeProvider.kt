package com.shabinder.common.di

import co.touchlab.kermit.Kermit
import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.database.Database
import io.ktor.client.*

expect class YoutubeProvider(
    httpClient: HttpClient,
    logger: Kermit,
    dir: Dir
) {
    suspend fun query(fullLink: String): PlatformQueryResult?
}