package com.shabinder.spotiflyer.ffmpeg

object FFmpeg {
    external fun testInit(): Long

    init {
        System.loadLibrary("spotiflyer-converter")
    }
}