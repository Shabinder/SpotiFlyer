package com.shabinder.spotiflyer.service

import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.translations.Strings

typealias Message = Pair<String, DownloadStatus>

val Message.title: String get() = first

val Message.downloadStatus: DownloadStatus get() = second

val Message.progress: String get() = when (downloadStatus) {
    is DownloadStatus.Downloading -> "-> ${(downloadStatus as DownloadStatus.Downloading).progress}%"
    is DownloadStatus.Converting -> "-> 100%"
    is DownloadStatus.Downloaded -> "-> ${Strings.downloadDone}"
    is DownloadStatus.Failed -> "-> ${Strings.failed()}"
    is DownloadStatus.Queued -> "-> ${Strings.queued()}"
    is DownloadStatus.NotDownloaded -> ""
}

val emptyMessage = Message("", DownloadStatus.NotDownloaded)

// `Progress` is not being shown because we don't get get consistent Updates from Download Fun ,
//  all Progress data is emitted all together from fun
fun Message.asString(): String {
    val statusString = when (downloadStatus) {
        is DownloadStatus.Downloading -> Strings.downloading()
        is DownloadStatus.Converting -> Strings.processing()
        else -> ""
    }
    return "$statusString $title ${""/*progress*/}".trim()
}

fun List<Message>.getEmpty(): MutableList<Message> = java.util.Collections.synchronizedList(MutableList(size) { emptyMessage })
