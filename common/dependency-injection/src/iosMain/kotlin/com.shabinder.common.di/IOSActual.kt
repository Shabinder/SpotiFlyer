package com.shabinder.common.di

import com.shabinder.common.models.AllPlatforms
import com.shabinder.common.models.TrackDetails
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

actual fun queryActiveTracks() {}

actual fun openPlatform(packageID: String, platformLink: String) {}

actual fun shareApp() {}

actual fun giveDonation() {}

actual val dispatcherIO: CoroutineDispatcher = Dispatchers.Default

actual val isInternetAvailable: Boolean
    get() = runBlocking { isInternetAccessible() }

actual val currentPlatform: AllPlatforms = AllPlatforms.Native

actual suspend fun downloadTracks(
    list: List<TrackDetails>,
    fetcher: FetchPlatformQueryResult,
    dir: Dir
) {

    // TODO
}