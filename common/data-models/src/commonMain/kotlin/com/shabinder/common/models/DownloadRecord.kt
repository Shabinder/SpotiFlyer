package com.shabinder.common.models

data class DownloadRecord(
    var id:Long = 0,
    var type:String,
    var name:String,
    var link:String,
    var coverUrl:String,
    var totalFiles:Long = 1,
)