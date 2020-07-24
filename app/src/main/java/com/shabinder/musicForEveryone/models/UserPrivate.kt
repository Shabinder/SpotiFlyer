package com.shabinder.musicForEveryone.models

data class UserPrivate(
    val country:String,
    var display_name: String,
    val email:String,
    var external_urls: Map<String?, String?>? = null,
    var followers: Followers? = null,
    var href: String? = null,
    var id: String? = null,
    var images: List<Image?>? = null,
    var product:String,
    var type: String? = null,
    var uri: String? = null)