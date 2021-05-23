package com.shabinder.common.models.saavn

import kotlinx.serialization.Serializable

@Serializable
data class SaavnSearchResult(
    val album: String? = "",
    val description: String,
    val id: String,
    val image: String,
    val title: String,
    val type: String,
    val url: String,
    val ctr: Int? = 0,
    val position: Int? = 0,
    val more_info: MoreInfo? = null,
)
