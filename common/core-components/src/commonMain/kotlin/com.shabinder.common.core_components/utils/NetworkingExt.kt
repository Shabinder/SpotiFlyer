package com.shabinder.common.core_components.utils

import com.shabinder.common.models.dispatcherIO
import com.shabinder.common.utils.globalJson
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.native.concurrent.SharedImmutable

suspend fun isInternetAccessible(): Boolean {
    return withContext(dispatcherIO) {
        try {
            ktorHttpClient.head<String>("https://open.spotify.com/")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

// If Fails returns Input Url
suspend inline fun HttpClient.getFinalUrl(
    url: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): String {
    return withContext(dispatcherIO) {
        runCatching {
            get<HttpResponse>(url,block).call.request.url.toString()
        }.getOrNull() ?: url
    }
}

fun createHttpClient(enableNetworkLogs: Boolean = false) = HttpClient {
    // https://github.com/Kotlin/kotlinx.serialization/issues/1450
    install(JsonFeature) {
        serializer = KotlinxSerializer(globalJson)
    }
    install(HttpTimeout) {
        socketTimeoutMillis = 520_000
        requestTimeoutMillis = 360_000
        connectTimeoutMillis = 360_000
    }
    // WorkAround for Freezing
    // Use httpClient.getData / httpClient.postData Extensions
    /*install(JsonFeature) {
        serializer = KotlinxSerializer(
            Json {
                isLenient = true
                ignoreUnknownKeys = true
            }
        )
    }*/
    if (enableNetworkLogs) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }
}


/*Client Active Throughout App's Lifetime*/
@SharedImmutable
val ktorHttpClient = HttpClient {}