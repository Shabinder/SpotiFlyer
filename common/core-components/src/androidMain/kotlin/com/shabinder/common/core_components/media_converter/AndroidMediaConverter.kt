package com.shabinder.common.core_components.media_converter

import android.content.Context
import android.util.Log
import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.SpotiFlyerException
import kotlinx.coroutines.delay
import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler
import nl.bravobit.ffmpeg.FFmpeg
import org.koin.dsl.bind
import org.koin.dsl.module


class AndroidMediaConverter(private val appContext: Context) : MediaConverter() {

    override suspend fun convertAudioFile(
        inputFilePath: String,
        outputFilePath: String,
        audioQuality: AudioQuality,
        progressCallbacks: (Long) -> Unit,
    ) = executeSafelyInPool {
        var progressing = true
        var error = ""
        var timeout = 600_000L * 2 // 20 min
        val progressDelayCheck = 500L
        // 192 is Default
        val audioBitrate =
            if (audioQuality == AudioQuality.UNKNOWN) 192 else audioQuality.kbps.toIntOrNull()
                ?: 192
        FFmpeg.getInstance(appContext).execute(
            arrayOf(
                "-i",
                inputFilePath,
                "-y", /*"-acodec", "libmp3lame",*/
                "-b:a",
                "${audioBitrate}k",
                "-vn",
                outputFilePath
            ), object : ExecuteBinaryResponseHandler() {
                override fun onSuccess(message: String?) {
                    //Log.d("FFmpeg Command", "Success $message")
                    progressing = false
                    Log.d("FFmpeg Success", "$message")
                }

                override fun onProgress(message: String?) {
                    super.onProgress(message)
                    Log.d("FFmpeg Progress", "Progress $message  ---  $inputFilePath")
                }

                override fun onFailure(message: String?) {
                    error = "Failed: $message $inputFilePath"
                    error += "FFmpeg Support" + FFmpeg.getInstance(appContext).isSupported.toString()
                    Log.d("FFmpeg Error", error)
                    progressing = false
                }
            }
        )
        while (progressing) {
            if (timeout < 0) throw SpotiFlyerException.MP3ConversionFailed("$error Conversion Timeout for $inputFilePath")
            delay(progressDelayCheck)
            timeout -= progressDelayCheck
        }
        if(error.isNotBlank()) throw SpotiFlyerException.MP3ConversionFailed(error)
        // Return output file path after successful conversion
        outputFilePath
    }
}

internal actual fun mediaConverterModule() = module {
    single { AndroidMediaConverter(get()) } bind MediaConverter::class
}