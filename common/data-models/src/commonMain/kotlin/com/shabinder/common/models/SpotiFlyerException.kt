package com.shabinder.common.models

import com.shabinder.common.translations.Strings

sealed class SpotiFlyerException(override val message: String) : Exception(message) {

    data class FeatureNotImplementedYet(override val message: String = Strings.featureUnImplemented()) : SpotiFlyerException(message)
    data class NoInternetException(override val message: String = Strings.checkInternetConnection()) : SpotiFlyerException(message)

    data class MP3ConversionFailed(
        val extraInfo: String? = null,
        override val message: String = "${Strings.mp3ConverterBusy()} \nCAUSE:$extraInfo"
    ) : SpotiFlyerException(message)

    data class UnknownReason(
        val exception: Throwable? = null,
        override val message: String = Strings.unknownError()
    ) : SpotiFlyerException(message)

    data class NoMatchFound(
        val trackName: String? = null,
        override val message: String = "$trackName : ${Strings.noMatchFound()}"
    ) : SpotiFlyerException(message)

    data class YoutubeLinkNotFound(
        val videoID: String? = null,
        override val message: String = "${Strings.noLinkFound()}: $videoID"
    ) : SpotiFlyerException(message)

    data class DownloadLinkFetchFailed(
        val trackName: String,
        val jioSaavnError: Throwable,
        val ytMusicError: Throwable,
        override val message: String = "${Strings.noLinkFound()}: $trackName," +
            " \n YtMusic Error's StackTrace: ${ytMusicError.stackTraceToString()} \n " +
            " \n JioSaavn Error's StackTrace: ${jioSaavnError.stackTraceToString()} \n "
    ) : SpotiFlyerException(message)

    data class LinkInvalid(
        val link: String? = null,
        override val message: String = "${Strings.linkNotValid()}\n ${link ?: ""}"
    ) : SpotiFlyerException(message)
}
