package com.shabinder.common.models.soundcloud


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PublisherMetadata(
    @SerialName("album_title")
    val albumTitle: String = "",
    val artist: String = "",
    @SerialName("contains_music")
    val containsMusic: Boolean = false,
    val id: Int = 0,
    val isrc: String = "",
    val publisher: String = "",
    @SerialName("release_title")
    val releaseTitle: String = "",
    @SerialName("upc_or_ean")
    val upcOrEan: String = "",
    val urn: String = "",
    @SerialName("writer_composer")
    val writerComposer: String = ""
)