package com.shabinder.musicForEveryone.models

data class Album(
    var album_type: String? = null,
    var artists: List<Artist?>? = null,
    var available_markets: List<String?>? = null,
    var copyrights: List<Copyright?>? = null,
    var external_ids: Map<String?, String?>? = null,
    var external_urls: Map<String?, String?>? = null,
    var genres: List<String?>? = null,
    var href: String? = null,
    var id: String? = null,
    var images: List<Image?>? = null,
    var label :String? = null,
    var name: String? = null,
    var popularity: Int? = null,
    var release_date: String? = null,
    var release_date_precision: String? = null,
    var tracks: PagingObject<Track?>? = null,
    var type: String? = null,
    var uri: String? = null)