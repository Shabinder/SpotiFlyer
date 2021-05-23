package jiosaavn.models

import kotlinx.serialization.Serializable

@Serializable
data class SaavnSong(
    val `320kbps`: Boolean,
    val album: String,
    val album_url: String? = null,
    val albumid: String? = null,
    val artistMap: Map<String, String>,
    val copyright_text: String? = null,
    val duration: String,
    val encrypted_media_path: String,
    val encrypted_media_url: String,
    val explicit_content: Int = 0,
    val has_lyrics: Boolean = false,
    val id: String,
    val image: String,
    val label: String? = null,
    val label_url: String? = null,
    val language: String,
    val lyrics_snippet: String? = null,
    val media_preview_url: String? = null,
    val media_url: String? = null, // Downloadable M4A Link
    val music: String,
    val music_id: String,
    val origin: String? = null,
    val perma_url: String? = null,
    val play_count: Int = 0,
    val primary_artists: String,
    val primary_artists_id: String,
    val release_date: String, // Format - 2021-05-04
    val singers: String,
    val song: String, // title
    val starring: String? = null,
    val type: String = "",
    val vcode: String? = null,
    val vlink: String? = null,
    val year: String
)
