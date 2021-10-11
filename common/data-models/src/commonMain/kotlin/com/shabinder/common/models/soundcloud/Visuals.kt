package com.shabinder.common.models.soundcloud


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Visuals(
    val enabled: Boolean = false,
    //val tracking: Any = Any(),
    val urn: String = "",
    val visuals: List<Visual> = listOf()
)