package com.shabinder.musicForEveryone.models

import com.squareup.moshi.Json

data class Playlist(
    @Json(name = "collaborative")var is_collaborative: Boolean? = null,
    var description: String? = null,
    var external_urls: Map<String?, String?>? = null,
    var followers: Followers? = null,
    var href: String? = null,
    var id: String? = null,
    var images: List<Image?>? = null,
    var name: String? = null,
    var owner: UserPublic? = null,
    @Json(name = "public")var is_public: Boolean? = null,
    var snapshot_id: String? = null,
    var tracks: PagingObject<PlaylistTrack?>? = null,
    var type: String? = null,
    var uri: String? = null)