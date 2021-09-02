package nl.bravobit.ffmpeg

import android.content.Context

object FFmpegConfig {
    fun versionFFmpeg(context: Context) {
        FFmpeg.getInstance(context).execute(arrayOf("-version"), object : ExecuteBinaryResponseHandler() {
            override fun onSuccess(message: String) {
                Log.d(message)
            }

            override fun onProgress(message: String) {
                Log.d(message)
            }
        })
    }

    fun codecsFFmpeg(context: Context) {
        FFmpeg.getInstance(context).execute(arrayOf("-codecs"), object : ExecuteBinaryResponseHandler() {
            override fun onSuccess(message: String) {
                Log.d(message)
            }

            override fun onProgress(message: String) {
                Log.d(message)
            }
        })
    }

    fun versionFFprobe(context: Context) {
        Log.d("version ffprobe")
        FFprobe.getInstance(context).execute(arrayOf("-version"), object : ExecuteBinaryResponseHandler() {
            override fun onSuccess(message: String) {
                Log.d(message)
            }

            override fun onProgress(message: String) {
                Log.d(message)
            }
        })
    }
}