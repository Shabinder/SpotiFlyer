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
                }

                override fun onProgress(message: String?) {
                    super.onProgress(message)
                    Log.d("FFmpeg Progress", "Progress $message  ---  $inputFilePath")
                }

                override fun onFailure(message: String?) {
                    Log.d("FFmpeg Command", "Failed $message")
                    progressing = false
                    throw SpotiFlyerException.MP3ConversionFailed(message = "Android FFmpeg Failed: $message")
                }
            }
        )
        while (progressing) {
            if (timeout < 0) throw SpotiFlyerException.MP3ConversionFailed("Conversion Timeout for $inputFilePath")
            delay(progressDelayCheck)
            timeout -= progressDelayCheck
        }
        // Return output file path after successful conversion
        outputFilePath
    }
}

internal actual fun mediaConverterModule() = module {
    single { AndroidMediaConverter(get()) } bind MediaConverter::class
}