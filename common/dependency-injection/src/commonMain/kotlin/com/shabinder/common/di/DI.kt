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

package com.shabinder.common.di

import co.touchlab.kermit.Kermit
import com.shabinder.common.database.databaseModule
import com.shabinder.common.database.getLogger
import com.shabinder.common.di.providers.GaanaProvider
import com.shabinder.common.di.providers.SpotifyProvider
import com.shabinder.common.di.providers.YoutubeMp3
import com.shabinder.common.di.providers.YoutubeMusic
import com.shabinder.common.models.NativeAtomicReference
import io.ktor.client.HttpClient
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import kotlin.native.concurrent.SharedImmutable

fun initKoin(enableNetworkLogs: Boolean = false, appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule(enableNetworkLogs = enableNetworkLogs), databaseModule())
    }

// Called by IOS
fun initKoin() = initKoin(enableNetworkLogs = false) { }

fun commonModule(enableNetworkLogs: Boolean) = module {
    single { createHttpClient(enableNetworkLogs = enableNetworkLogs) }
    single { Dir(get(), get()) }
    single { Kermit(getLogger()) }
    single { TokenStore(get(), get()) }
    single { YoutubeMusic(get(), get()) }
    single { SpotifyProvider(get(), get(), get()) }
    single { GaanaProvider(get(), get(), get()) }
    single { YoutubeProvider(get(), get(), get()) }
    single { YoutubeMp3(get(), get(), get()) }
    single { FetchPlatformQueryResult(get(), get(), get(), get(), get(), get()) }
}

@SharedImmutable
val kotlinxSerializer = KotlinxSerializer(
    Json {
        isLenient = true
        ignoreUnknownKeys = true
    }
)

fun createHttpClient(enableNetworkLogs: Boolean = false, serializer: KotlinxSerializer = kotlinxSerializer) = HttpClient {
    install(JsonFeature) {
        this.serializer = serializer
    }
    // Timeout
    install(HttpTimeout) {
        requestTimeoutMillis = 15000L
        connectTimeoutMillis = 15000L
        socketTimeoutMillis = 15000L
    }
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
