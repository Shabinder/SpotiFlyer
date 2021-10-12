package com.shabinder.common.di

import com.shabinder.common.models.dispatcherIO
import com.shabinder.common.providers.spotify.SpotifyProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.dsl.module

class ApplicationInit(
    private val spotifyProvider: SpotifyProvider,
) {
    companion object {
        private var isFirstLaunch = true
    }

    /*
    * Init Basic Necessary Items in here,
    *  will be called,
    *   Android / IOS: Splash Screen
    *   Desktop: App Startup
    * */
    @OptIn(DelicateCoroutinesApi::class)
    fun init() = GlobalScope.launch(dispatcherIO) {
        isFirstLaunch = false
        spotifyProvider.authenticateSpotifyClient()
    }
}

internal fun appInitModule() = module {
    single {
        ApplicationInit(get())
    }
}