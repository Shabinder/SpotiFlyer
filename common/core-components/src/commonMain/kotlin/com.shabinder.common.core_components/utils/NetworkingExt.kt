package com.shabinder.common.core_components.utils

import com.shabinder.common.models.dispatcherIO
import com.shabinder.common.utils.globalJson
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
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