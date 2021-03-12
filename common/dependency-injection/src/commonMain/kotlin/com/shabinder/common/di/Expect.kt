package com.shabinder.common.di

import com.shabinder.common.models.TrackDetails
import kotlinx.coroutines.CoroutineDispatcher

expect fun openPlatform(packageID:String, platformLink:String)

expect fun shareApp()

expect fun giveDonation()

expect val dispatcherIO: CoroutineDispatcher

expect val isInternetAvailable:Boolean

expect suspend fun downloadTracks(
    list: List<TrackDetails>,
    fetcher: FetchPlatformQueryResult,
    dir: Dir
)

expect fun queryActiveTracks()