/*
 * Copyright (c)  2021  Shabinder Singh
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.common.models

import com.shabinder.common.models.spotify.Source
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class TrackDetails(
    var title: String,
    var artists: List<String>,
    var durationSec: Int,
    var albumName: String? = null,
    var albumArtists: List<String> = emptyList(),
    var genre: List<String> = emptyList(),
    var trackNumber: Int? = null,
    var year: String? = null,
    var comment: String? = null,
    var lyrics: String? = null,
    var trackUrl: String? = null,
    var albumArtPath: String, // UriString in Android
    var albumArtURL: String,
    var source: Source,
    val progress: Int = 2,
    val downloadLink: String? = null,
    val downloaded: DownloadStatus = DownloadStatus.NotDownloaded,
    var audioQuality: AudioQuality = AudioQuality.KBPS192,
    var audioFormat: AudioFormat = AudioFormat.MP4,
    var outputFilePath: String, // UriString in Android
    var videoID: String? = null, // will be used for purposes like Downloadable Link || VideoID etc. based on Provider
) : Parcelable {
    val outputMp3Path get() = outputFilePath.substringBeforeLast(".") + ".mp3"
}

@Serializable
sealed class DownloadStatus : Parcelable {
    @Parcelize object Downloaded : DownloadStatus()
    @Parcelize data class Downloading(val progress: Int = 2) : DownloadStatus()
    @Parcelize object Queued : DownloadStatus()
    @Parcelize object NotDownloaded : DownloadStatus()
    @Parcelize object Converting : DownloadStatus()
    @Parcelize data class Failed(val error: Throwable) : DownloadStatus()
}
