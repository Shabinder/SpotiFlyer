package com.shabinder.common.models.saavn

import kotlinx.serialization.Serializable

@Serializable
data class SaavnPlaylist(
    val fan_count: Int? = 0,
    val firstname: String? = null,
    val follower_count: Long? = null,
    val image: String,
    val images: List<String>? = null,
    val last_updated: String,
    val lastname: String? = null,
    val list_count: String? = null,
    val listid: String? = null,
    val listname: String, // Title
    val perma_url: String,
    val songs: List<SaavnSong>,
    val sub_types: List<String>? = null,
    val type: String = "", // chart,etc
    val uid: String? = null,
)
