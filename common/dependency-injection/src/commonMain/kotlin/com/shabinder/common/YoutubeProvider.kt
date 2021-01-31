package com.shabinder.common

import co.touchlab.kermit.Kermit
import com.shabinder.database.Database
import io.ktor.client.*

expect class YoutubeProvider(
    httpClient: HttpClient,
    database: Database,
    logger: Kermit,
    dir: Dir
) {
    suspend fun query(fullLink: String): PlatformQueryResult?
}