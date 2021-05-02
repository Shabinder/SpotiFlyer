package com.shabinder.common.di

import com.shabinder.common.models.AllPlatforms
import com.shabinder.common.models.TrackDetails
import kotlinx.coroutines.Dispatchers

@SharedImmutable
actual val dispatcherIO = Dispatchers.Default

@SharedImmutable
actual val currentPlatform: AllPlatforms = AllPlatforms.Native

actual suspend fun downloadTracks(
    list: List<TrackDetails>,
    fetcher: FetchPlatformQueryResult,
    dir: Dir
) {
    // TODO
}