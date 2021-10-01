package com.shabinder.common.models.soundcloud


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Format(
    @SerialName("mime_type")
    val mimeType: String = "",
    val protocol: String = ""
) {
    val isProgressive get() = protocol == "progressive"
}