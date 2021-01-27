package com.shabinder.common

data class DownloadRecord(
    var id:Int = 0,
    var type:String,
    var name:String,
    var link:String,
    var coverUrl:String,
    var totalFiles:Int = 1,
)