package com.shabinder.common.di

import com.shabinder.common.models.TrackDetails

actual suspend fun downloadTracks(
    list: List<TrackDetails>,
    fetcher: FetchPlatformQueryResult,
    dir: Dir
) {

    // TODO
}