package com.shabinder.common.models.soundcloud


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("avatar_url")
    val avatarUrl: String = "",
    val badges: Badges = Badges(),
    val city: String = "",
    @SerialName("country_code")
    val countryCode: String = "",
    @SerialName("first_name")
    val firstName: String = "",
    @SerialName("followers_count")
    val followersCount: Int = 0,
    @SerialName("full_name")
    val fullName: String = "",
    val id: Int = 0,
    val kind: String = "",
    @SerialName("last_modified")
    val lastModified: String = "",
    @SerialName("last_name")
    val lastName: String = "",
    val permalink: String = "",
    @SerialName("permalink_url")
    val permalinkUrl: String = "",
    @SerialName("station_permalink")
    val stationPermalink: String = "",
    @SerialName("station_urn")
    val stationUrn: String = "",
    val uri: String = "",
    val urn: String = "",
    val username: String = "",
    val verified: Boolean = false
)