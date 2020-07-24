package com.shabinder.musicForEveryone.models

data class Artist(
    var external_urls: Map<String?, String?>? = null,
    var href: String? = null,
    var id: String? = null,
    var name: String? = null,
    var type: String? = null,
    var uri: String? = null)