package com.shabinder.common.di

import com.shabinder.common.models.TrackDetails
import kotlinx.coroutines.CoroutineDispatcher

expect fun openPlatform(packageID:String, platformLink:String)

expect fun shareApp()

expect fun giveDonation()

expect fun showPopUpMessage(text: String)

expect val dispatcherIO: CoroutineDispatcher

expect suspend fun downloadTracks(
    list: List<TrackDetails>,
    getYTIDBestMatch:suspend (String,TrackDetails)->String?,
    saveFileWithMetaData:suspend (mp3ByteArray:ByteArray, trackDetails: TrackDetails) -> Unit
)

expect fun queryActiveTracks()