package com.shabinder.common.di.youtubeMp3

import com.shabinder.common.di.gaana.corsApi
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/*
* site link: https://yt1s.com/youtube-to-mp3/en1
* Provides Direct Mp3 , No Need For FFmpeg
* */
interface Yt1sMp3 {

    val httpClient: HttpClient

    /*
    * Downloadable Mp3 Link for YT videoID.
    * */
    suspend fun getLinkFromYt1sMp3(videoID: String):String? =
        getConvertedMp3Link(videoID,getKey(videoID))?.get("dlink")?.jsonPrimitive?.toString()?.replace("\"", "")

    /*
    * POST:https://yt1s.com/api/ajaxSearch/index
    * Body Form= q:yt video link ,vt:format=mp3
    * */
    private suspend fun getKey(videoID:String):String{
        val response:JsonObject? = httpClient.post("${corsApi}https://yt1s.com/api/ajaxSearch/index"){
            body = FormDataContent(Parameters.build {
                append("q","https://www.youtube.com/watch?v=$videoID")
                append("vt","mp3")
            })
        }
        return response?.get("kc")?.jsonPrimitive.toString()
    }

    private suspend fun getConvertedMp3Link(videoID: String,key:String):JsonObject?{
        return httpClient.post("${corsApi}https://yt1s.com/api/ajaxConvert/convert"){
            body = FormDataContent(Parameters.build {
                append("vid", videoID)
                append("k",key)
            })
        }
    }
}