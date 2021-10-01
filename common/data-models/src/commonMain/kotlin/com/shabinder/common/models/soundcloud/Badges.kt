package com.shabinder.common.models.soundcloud


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Badges(
    val pro: Boolean = false,
    @SerialName("pro_unlimited")
    val proUnlimited: Boolean = false,
    val verified: Boolean = false
)