package com.shabinder.common.models

sealed class SpotiFlyerException(override val message: String): Exception(message) {

    data class FeatureNotImplementedYet(override val message: String = "Feature not yet implemented."): SpotiFlyerException(message)

    data class NoMatchFound(
        val trackName: String? = null,
        override val message: String = "$trackName : NO Match Found!"
    ): SpotiFlyerException(message)

    data class YoutubeLinkNotFound(
        val videoID: String? = null,
        override val message: String = "No Downloadable link found for videoID: $videoID"
    ): SpotiFlyerException(message)

    data class LinkInvalid(
        val link: String? = null,
        override val message: String = "Entered Link is NOT Valid!\n ${link ?: ""}"
    ): SpotiFlyerException(message)
}