package com.shabinder.common

import co.touchlab.kermit.Kermit
import com.shabinder.common.database.createDb
import com.shabinder.common.database.getLogger
import com.shabinder.common.providers.GaanaProvider
import com.shabinder.common.providers.SpotifyProvider
import com.shabinder.common.providers.YoutubeMusic
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(enableNetworkLogs: Boolean = false, appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule(enableNetworkLogs = enableNetworkLogs))
    }

fun commonModule(enableNetworkLogs: Boolean) = module {
    single { Dir() }
    single { createDb() }
    single { Kermit(getLogger()) }
    single { YoutubeMusic(get(),get()) }
    single { SpotifyProvider(get(),get(),get(),get()) }
    single { GaanaProvider(get(),get(),get(),get()) }
    single { YoutubeProvider(get(),get(),get(),get()) }
    single { createHttpClient(enableNetworkLogs = enableNetworkLogs) }
}

val kotlinxSerializer = KotlinxSerializer( Json {
    isLenient = true
    ignoreUnknownKeys = true
})

fun createHttpClient(enableNetworkLogs: Boolean,serializer: KotlinxSerializer = kotlinxSerializer) = HttpClient {
    install(JsonFeature) {
        this.serializer = serializer
    }
    if (enableNetworkLogs) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }
}