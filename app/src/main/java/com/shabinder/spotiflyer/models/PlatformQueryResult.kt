package com.shabinder.spotiflyer.models

import com.shabinder.spotiflyer.models.spotify.Source

data class PlatformQueryResult(
    var folderType: String,
    var subFolder: String,
    var title: String,
    var coverUrl: String,
    var trackList: List<TrackDetails>,
    var source: Source
)