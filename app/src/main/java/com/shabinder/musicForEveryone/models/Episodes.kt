package com.shabinder.musicForEveryone.models

data class Episodes(
    var audio_preview_url:String?,
    var description:String?,
    var duration_ms:Int?,
    var explicit:Boolean?,
    var external_urls:Map<String,String>?,
    var href:String?,
    var id:String?,
    var images:List<Image?>?,
    var is_externally_hosted:Boolean?,
    var is_playable:Boolean?,
    var language:String?,
    var languages:List<String?>?,
    var name:String?,
    var release_date:String?,
    var release_date_precision:String?,
    var type:String?,
    var uri:String
)