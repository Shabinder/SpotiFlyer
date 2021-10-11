package com.shabinder.common.providers

import com.shabinder.common.providers.gaana.GaanaProvider
import com.shabinder.common.providers.saavn.SaavnProvider
import com.shabinder.common.providers.sound_cloud.SoundCloudProvider
import com.shabinder.common.providers.spotify.SpotifyProvider
import com.shabinder.common.providers.spotify.token_store.TokenStore
import com.shabinder.common.providers.youtube.YoutubeProvider
import com.shabinder.common.providers.youtube_music.YoutubeMusic
import com.shabinder.common.providers.youtube_to_mp3.requests.YoutubeMp3
import org.koin.dsl.module

@Suppress("UNUSED_PARAMETER")
fun providersModule(enableNetworkLogs: Boolean) = module {
    single { TokenStore(get(), get()) }
    single { SpotifyProvider(get(), get(), get()) }
    single { GaanaProvider(get(), get(), get()) }
    single { SaavnProvider(get(), get(), get()) }
    single { YoutubeProvider(get(), get(), get()) }
    single { SoundCloudProvider(get(), get(), get()) }
    single { YoutubeMp3(get(), get()) }
    single { YoutubeMusic(get(), get(), get(), get(), get()) }
    single { FetchPlatformQueryResult(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}
