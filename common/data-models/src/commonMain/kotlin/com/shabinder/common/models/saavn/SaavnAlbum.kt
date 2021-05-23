package com.shabinder.common.models.saavn

import kotlinx.serialization.Serializable

@Serializable
data class SaavnAlbum(
    val albumid: String,
    val image: String,
    val name: String,
    val perma_url: String,
    val primary_artists: String,
    val primary_artists_id: String,
    val release_date: String,
    val songs: List<SaavnSong>,
    val title: String,
    val year: String
)
