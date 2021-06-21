/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.common.di.providers

import co.touchlab.kermit.Kermit
import com.shabinder.common.di.youtubeMp3.Yt1sMp3
import com.shabinder.common.models.corsApi
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import com.shabinder.common.models.event.coroutines.map
import io.ktor.client.*

interface YoutubeMp3: Yt1sMp3 {

    companion object {
        operator fun invoke(
            client: HttpClient,
            logger: Kermit
        ): YoutubeMp3 {
            return object : YoutubeMp3 {
                override val httpClient: HttpClient = client
                override val logger: Kermit = logger
            }
        }
    }

    suspend fun getMp3DownloadLink(videoID: String): SuspendableEvent<String,Throwable> = getLinkFromYt1sMp3(videoID).map {
        corsApi + it
    }
}
