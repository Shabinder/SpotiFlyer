package com.shabinder.common.models.saavn

import kotlinx.serialization.Serializable

@Serializable
data class MoreInfo(
    val language: String,
    val primary_artists: String,
    val singers: String,
)
