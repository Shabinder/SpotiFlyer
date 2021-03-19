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
import com.shabinder.common.di.Dir
import com.shabinder.common.di.currentPlatform
import com.shabinder.common.di.youtubeMp3.Yt1sMp3
import com.shabinder.common.models.AllPlatforms
import io.ktor.client.HttpClient

class YoutubeMp3(
    override val httpClient: HttpClient,
    private val logger: Kermit,
    private val dir: Dir,
) : Yt1sMp3 {
    suspend fun getMp3DownloadLink(videoID: String): String? = getLinkFromYt1sMp3(videoID)?.let {
        if (currentPlatform is AllPlatforms.Js/* && corsProxy !is CorsProxy.PublicProxyWithExtension*/)
            "https://kind-grasshopper-73.telebit.io/cors/$it"
        else it
    }
}
