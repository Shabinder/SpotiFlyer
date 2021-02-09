package com.shabinder.common.di

import com.shabinder.common.models.TrackDetails

expect class Picture

expect fun openPlatform(platformID:String ,platformLink:String)

expect fun shareApp()

expect fun giveDonation()

expect fun downloadTracks(list: List<TrackDetails>)