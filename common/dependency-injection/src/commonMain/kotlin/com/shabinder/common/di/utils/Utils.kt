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

package com.shabinder.common.di.utils

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.native.concurrent.SharedImmutable
import kotlin.native.concurrent.ThreadLocal

/*
* WorkAround: https://github.com/Kotlin/kotlinx.serialization/issues/1450
* */
@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T: Any> HttpClient.getData(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): T {
    val response = get<HttpResponse> {
        url.takeFrom(urlString)
        block()
    }
    val jsonBody = response.readText()
    return json.decodeFromString(T::class.serializer(),jsonBody)
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T: Any> HttpClient.postData(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): T {
    val response = post<HttpResponse> {
        url.takeFrom(urlString)
        block()
    }
    val jsonBody = response.readText()
    return json.decodeFromString(T::class.serializer(),jsonBody)
}

@ThreadLocal
val json by lazy { Json {
    isLenient = true
    ignoreUnknownKeys = true
} }

/**
 * Removing Illegal Chars from File Name
 * **/
fun removeIllegalChars(fileName: String): String {
    val illegalCharArray = charArrayOf(
        '/',
        '\n',
        '\r',
        '\t',
        '\u0000',
        '\u000C',
        '`',
        '?',
        '*',
        '\\',
        '<',
        '>',
        '|',
        '\"',
        '.',
        '-',
        '\''
    )

    var name = fileName
    for (c in illegalCharArray) {
        name = fileName.replace(c, '_')
    }
    name = name.replace("\\s".toRegex(), "_")
    name = name.replace("\\)".toRegex(), "")
    name = name.replace("\\(".toRegex(), "")
    name = name.replace("\\[".toRegex(), "")
    name = name.replace("]".toRegex(), "")
    name = name.replace("\\.".toRegex(), "")
    name = name.replace("\"".toRegex(), "")
    name = name.replace("\'".toRegex(), "")
    name = name.replace(":".toRegex(), "")
    name = name.replace("\\|".toRegex(), "")
    return name
}
