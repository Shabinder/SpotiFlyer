package com.shabinder.musicForEveryone.models

data class PagingObject<T>(
    var href: String? = null,
    var items: List<T>? = null,
    var limit: Int = 0,
    var next: String? = null,
    var offset: Int = 0,
    var previous: String? = null,
    var total: Int = 0)