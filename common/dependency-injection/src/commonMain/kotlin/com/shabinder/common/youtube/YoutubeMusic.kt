package com.shabinder.common.youtube

import com.shabinder.common.gaana.httpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

private const val apiKey = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30"

interface YoutubeMusic {

    suspend fun getYoutubeMusicResponse(query: String):String{
        return httpClient.post("https://music.youtube.com/youtubei/v1/search?alt=json&key=$apiKey"){
            contentType(ContentType.Application.Json)
            headers{
                //append("Content-Type"," application/json")
                append("Referer"," https://music.youtube.com/search")
            }
            body = buildJsonObject {
                putJsonObject("context"){
                    putJsonObject("client"){
                        put("clientName" ,"WEB_REMIX")
                        put("clientVersion" ,"0.1")
                    }
                }
                put("query",query)
            }
        }
    }
}
/*
fun makeJsonBody(query: String):JsonObject{
    val client = JsonObject()
    client["clientName"] = "WEB_REMIX"
    client["clientVersion"] = "0.1"

    val context = JsonObject()
    context["client"] = client

    val mainObject = JsonObject()
    mainObject["context"] = context
    mainObject["query"] = query

    return mainObject
}*/
