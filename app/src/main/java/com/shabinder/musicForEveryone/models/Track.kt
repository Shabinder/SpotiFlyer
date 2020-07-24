package com.shabinder.musicForEveryone.models

data class Track(
    var artists: List<Artist?>? = null,
    var available_markets: List<String?>? = null,
    var is_playable: Boolean? = null,
    var linked_from: LinkedTrack? = null,
    var disc_number: Int = 0,
    var duration_ms: Long = 0,
    var explicit: Boolean? = null,
    var external_urls: Map<String?, String?>? = null,
    var href: String? = null,
    var id: String? = null,
    var name: String? = null,
    var preview_url: String? = null,
    var track_number: Int = 0,
    var type: String? = null,
    var uri: String? = null,
    var album: Album? = null,
    var external_ids: Map<String?, String?>? = null,
    var popularity: Int? = null)