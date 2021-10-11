package com.shabinder.common.models.saavn

import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.DownloadStatus
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class SaavnSong @OptIn(ExperimentalSerializationApi::class) constructor(
    @JsonNames("320kbps") val is320Kbps: Boolean,
    val album: String,
    val album_url: String? = null,
    val albumid: String? = null,
    val artistMap: Map<String, String>,
    val copyright_text: String? = null,
    val duration: String,
    val encrypted_media_path: String,
    val encrypted_media_url: String,
//    val explicit_content: Int = 0,
    val has_lyrics: Boolean = false,
    val id: String,
    val image: String = "",
    val label: String? = null,
    val label_url: String? = null,
    val language: String,
    val lyrics_snippet: String? = null,
    val lyrics: String? = null,
    val media_preview_url: String? = null,
    val media_url: String? = null, // Downloadable M4A Link
    val music: String,
    val music_id: String,
    val origin: String? = null,
    val perma_url: String? = null,
//    val play_count: Int = 0,
    val primary_artists: String,
    val primary_artists_id: String,
    val release_date: String, // Format - 2021-05-04
    val singers: String,
    val song: String, // title
    val starring: String? = null,
    val type: String = "",
    val vcode: String? = null,
    val vlink: String? = null,
    val year: String,
    var downloaded: DownloadStatus = DownloadStatus.NotDownloaded
) {
    val audioQuality get() = if (is320Kbps) AudioQuality.KBPS320 else AudioQuality.KBPS160
}
