package com.shabinder.common.providers

import com.shabinder.common.models.TrackDetails
import com.shabinder.common.providers.utils.CommonUtils
import com.shabinder.common.providers.utils.SpotifyUtils
import com.shabinder.common.providers.utils.SpotifyUtils.toTrackDetailsList
import io.github.shabinder.runBlocking
import kotlin.test.Test

class TestSpotifyTrackMatching {

    companion object {
        const val SPOTIFY_TRACK_ID = "58f4twRnbZOOVUhMUpplJ4"
        const val SPOTIFY_TRACK_LINK = "https://open.spotify.com/track/$SPOTIFY_TRACK_ID?si=e45de595053e4ee2"
        const val EXPECTED_YT_VIDEO_ID = "VNs_cCtdbPc"
    }

    private val spotifyToken: String?
//        get() = null
    get() = "BQB41HqrLcrh5eRYaL97GvaH6tRe-1EktQ8VGTWUQuFnYVWBEoTcF7T_8ogqVn1GHl9HCcMiQ0HBT-ybC74"

    @Test
    fun matchVideo() = runBlocking {
        val spotifyRequests = SpotifyUtils.getSpotifyRequests(spotifyToken)

        val trackDetails: TrackDetails = spotifyRequests.getTrack(SPOTIFY_TRACK_ID).toTrackDetailsList()
        println("TRACK_DETAILS: $trackDetails")

//        val matched = CommonUtils.youtubeMusic.getYTTracks(CommonUtils.getYTQueryString(trackDetails))
//        println("YT-MATCHES: \n ${matched.component1()?.joinToString("\n")} \n")
        val ytMatch = CommonUtils.youtubeMusic.findMp3SongDownloadURLYT(trackDetails)
        println("YT MATCH: $ytMatch")
    }
}