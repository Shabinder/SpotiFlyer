package com.shabinder.common.models.soundcloud


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Visual(
    @SerialName("entry_time")
    val entryTime: Int = 0,
    val urn: String = "",
    @SerialName("visual_url")
    val visualUrl: String = ""
)