package com.shabinder.common

import co.touchlab.kermit.Kermit
import com.shabinder.database.DownloadRecordDatabase
import io.ktor.client.*

expect class YoutubeProvider(
    httpClient: HttpClient,
    database: DownloadRecordDatabase,
    logger: Kermit,
    dir: Dir
) {
    suspend fun query(fullLink: String): PlatformQueryResult?
}