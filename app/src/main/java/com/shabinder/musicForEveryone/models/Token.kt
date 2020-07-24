package com.shabinder.musicForEveryone.models

data class Token(
    var access_token:String,
    var token_type:String,
    var expires_in:Int
)