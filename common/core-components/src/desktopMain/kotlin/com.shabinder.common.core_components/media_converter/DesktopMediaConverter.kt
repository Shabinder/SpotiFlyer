package com.shabinder.common.core_components.media_converter

import com.github.kokorin.jaffree.ffmpeg.FFmpeg
import com.github.kokorin.jaffree.ffmpeg.UrlInput
import com.github.kokorin.jaffree.ffmpeg.UrlOutput
import com.shabinder.common.models.AudioQuality
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.io.path.Path

class DesktopMediaConverter : MediaConverter() {

    override suspend fun convertAudioFile(
        inputFilePath: String,
        outputFilePath: String,
        audioQuality: AudioQuality,
        progressCallbacks: (Long) -> Unit,
    ) = executeSafelyInPool {
        val audioBitrate =
            if (audioQuality == AudioQuality.UNKNOWN) 192 else audioQuality.kbps.toIntOrNull()
                ?: 192
        FFmpeg.atPath().run {
            addInput(UrlInput.fromUrl(inputFilePath))
            setOverwriteOutput(true)
            if (audioQuality != AudioQuality.UNKNOWN) {
                addArguments("-b:a", "${audioBitrate}k")
            }
            addArguments("-acodec", "libmp3lame")
            addArgument("-vn")
            addOutput(UrlOutput.toUrl(outputFilePath))
            setProgressListener {
                progressCallbacks(it.timeMillis)
            }
            execute()

            return@run outputFilePath
        }
    }
}

internal actual fun mediaConverterModule() = module {
    single { DesktopMediaConverter() } bind MediaConverter::class
}