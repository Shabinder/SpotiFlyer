package com.shabinder.common.di

import co.touchlab.kermit.Kermit
import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.database.Database
import io.ktor.client.*

actual class YoutubeProvider actual constructor(
    httpClient: HttpClient,
    logger: Kermit,
    dir: Dir
) {
    actual suspend fun query(fullLink: String): PlatformQueryResult? {
        return null // TODO
    }
}