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

package com.shabinder.common.di.youtubeMp3

import co.touchlab.kermit.Kermit
import com.shabinder.common.di.gaana.corsApi
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.http.Parameters
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/*
* site link: https://yt1s.com/youtube-to-mp3/en1
* Provides Direct Mp3 , No Need For FFmpeg
* */
interface Yt1sMp3 {

    val httpClient: HttpClient
    val logger: Kermit
    /*
    * Downloadable Mp3 Link for YT videoID.
    * */
    suspend fun getLinkFromYt1sMp3(videoID: String): String? =
        getConvertedMp3Link(videoID, getKey(videoID))?.get("dlink")?.jsonPrimitive?.toString()?.replace("\"", "")

    /*
    * POST:https://yt1s.com/api/ajaxSearch/index
    * Body Form= q:yt video link ,vt:format=mp3
    * */
    private suspend fun getKey(videoID: String): String {
        val response: JsonObject? = httpClient.post("${corsApi}https://yt1s.com/api/ajaxSearch/index") {
            body = FormDataContent(
                Parameters.build {
                    append("q", "https://www.youtube.com/watch?v=$videoID")
                    append("vt", "mp3")
                }
            )
        }
        return response?.get("kc")?.jsonPrimitive.toString()
    }

    private suspend fun getConvertedMp3Link(videoID: String, key: String): JsonObject? {
        return httpClient.post("${corsApi}https://yt1s.com/api/ajaxConvert/convert") {
            body = FormDataContent(
                Parameters.build {
                    append("vid", videoID)
                    append("k", key)
                }
            )
        }
    }
}
