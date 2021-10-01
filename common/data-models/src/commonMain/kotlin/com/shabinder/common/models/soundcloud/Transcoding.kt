package com.shabinder.common.models.soundcloud


import com.shabinder.common.models.AudioFormat
import kotlinx.serialization.Serializable

@Serializable
data class Transcoding(
    val duration: Int = 0,
    val format: Format = Format(),
    val preset: String = "",
    val quality: String = "", //sq == 128kbps //hq == 256kbps
    val snipped: Boolean = false,
    val url: String = ""
) {
    val audioFormat: AudioFormat = when {
        preset.contains("mp3") -> AudioFormat.MP3
        preset.contains("aac") || preset.contains("m4a") -> AudioFormat.MP4
        preset.contains("flac") -> AudioFormat.FLAC
        else -> AudioFormat.UNKNOWN
    }
}