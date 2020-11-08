/*
 * Copyright (C)  2020  Shabinder Singh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer.networking

import com.beust.klaxon.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


const val apiKey = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30"

interface YoutubeMusicApi {

    @Headers("Content-Type: application/json", "Referer: https://music.youtube.com/search")
    @POST("search?alt=json&key=$apiKey")
    fun getYoutubeMusicResponse(@Body text: String): Call<String>
}

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
}