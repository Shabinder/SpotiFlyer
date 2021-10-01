package com.shabinder.common.models.soundcloud


import kotlinx.serialization.Serializable

@Serializable
data class Media(
    val transcodings: List<Transcoding> = emptyList()
)