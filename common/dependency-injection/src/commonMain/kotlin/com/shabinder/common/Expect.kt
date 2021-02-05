package com.shabinder.common

import co.touchlab.kermit.Kermit
import com.shabinder.common.utils.removeIllegalChars

expect class Picture

expect fun openPlatform(platformID:String ,platformLink:String)

expect fun shareApp()

expect fun giveDonation()

expect fun downloadTracks(list: List<TrackDetails>)