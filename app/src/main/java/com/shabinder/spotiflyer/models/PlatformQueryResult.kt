package com.shabinder.spotiflyer.models

data class PlatformQueryResult(
    var folderType: String,
    var subFolder: String,
    var title: String,
    var coverUrl: String,
    var trackList: List<TrackDetails>
)