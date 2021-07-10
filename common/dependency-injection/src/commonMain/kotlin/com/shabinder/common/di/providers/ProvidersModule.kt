package com.shabinder.common.di.providers

import com.shabinder.common.di.FetchPlatformQueryResult
import com.shabinder.common.di.providers.requests.audioToMp3.AudioToMp3
import org.koin.dsl.module

fun providersModule() = module {
    single { AudioToMp3(get(), get()) }
    single { SpotifyProvider(get(), get(), get()) }
    single { GaanaProvider(get(), get(), get()) }
    single { SaavnProvider(get(), get(), get(), get()) }
    single { YoutubeProvider(get(), get(), get()) }
    single { YoutubeMp3(get(), get()) }
    single { YoutubeMusic(get(), get(), get(), get(), get()) }
    single { FetchPlatformQueryResult(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}