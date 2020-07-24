package com.shabinder.musicForEveryone.models

data class PlaylistTrack(
    var added_at: String? = null,
    var added_by: UserPublic? = null,
    var track: Track? = null,
    var is_local: Boolean? = null)