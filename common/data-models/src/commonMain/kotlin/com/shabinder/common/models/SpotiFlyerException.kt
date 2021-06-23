package com.shabinder.common.models

sealed class SpotiFlyerException(override val message: String): Exception(message) {

    data class FeatureNotImplementedYet(override val message: String = "Feature not yet implemented."): SpotiFlyerException(message)
    data class NoInternetException(override val message: String = "Check Your Internet Connection"): SpotiFlyerException(message)

    data class MP3ConversionFailed(
        val extraInfo:String? = null,
        override val message: String = "MP3 Converter unreachable, probably BUSY ! \nCAUSE:$extraInfo"
    ): SpotiFlyerException(message)

    data class UnknownReason(
        val exception: Throwable? = null,
        override val message: String = "Unknown Error"
    ): SpotiFlyerException(message)

    data class NoMatchFound(
        val trackName: String? = null,
        override val message: String = "$trackName : NO Match Found!"
    ): SpotiFlyerException(message)

    data class YoutubeLinkNotFound(
        val videoID: String? = null,
        override val message: String = "No Downloadable link found for videoID: $videoID"
    ): SpotiFlyerException(message)

    data class DownloadLinkFetchFailed(
        val trackName: String,
        val jioSaavnError: Throwable,
        val ytMusicError: Throwable,
        override val message: String = "No Downloadable link found for track: $trackName," +
                " \n JioSaavn Error's StackTrace: ${jioSaavnError.stackTraceToString()} \n " +
                " \n YtMusic Error's StackTrace: ${ytMusicError.stackTraceToString()} \n "
    ): SpotiFlyerException(message)

    data class LinkInvalid(
        val link: String? = null,
        override val message: String = "Entered Link is NOT Valid!\n ${link ?: ""}"
    ): SpotiFlyerException(message)
}