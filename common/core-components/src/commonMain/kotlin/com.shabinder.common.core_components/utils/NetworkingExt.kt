package com.shabinder.common.core_components.utils

import com.shabinder.common.models.dispatcherIO
import com.shabinder.common.utils.globalJson
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
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
            get<HttpResponse>(url, block).call.request.url.toString()
        }.getOrNull() ?: url
    }
}

fun createHttpClient(enableNetworkLogs: Boolean = false) = buildHttpClient {
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

expect fun buildHttpClient(extraConfig: HttpClientConfig<*>.() -> Unit): HttpClient

/*Client Active Throughout App's Lifetime*/
@SharedImmutable
private val ktorHttpClient = HttpClient {}