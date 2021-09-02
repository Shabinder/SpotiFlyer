package com.shabinder.spotiflyer.ffmpeg

import android.util.Log

object AndroidFFmpeg {
    /**
     *
     * Run transcode_aac from doc/examples.
     *
     * @return zero if transcoding was successful
     */
    @JvmStatic
    external fun runTranscode(inFilename: String?, outFilename: String?, audioBitrate: Int): Int

    init {
        Log.i("FFmpeg", "Loading mobile-ffmpeg.")
        System.loadLibrary("avutil")
        System.loadLibrary("swscale")
        System.loadLibrary("swresample")
        System.loadLibrary("avcodec")
        System.loadLibrary("avformat")
        System.loadLibrary("avfilter")
        System.loadLibrary("avdevice")
        //System.loadLibrary("avresample")
        System.loadLibrary("spotiflyer-ffmpeg")
    }
}