package com.shabinder.musicForEveryone.models

data class UserPublic(
    var display_name: String? = null,
    var external_urls: Map<String?, String?>? = null,
    var followers: Followers? = null,
    var href: String? = null,
    var id: String? = null,
    var images: List<Image?>? = null,
    var type: String? = null,
    var uri: String? = null)