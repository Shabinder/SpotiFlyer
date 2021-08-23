package com.shabinder.common.core_components.media_converter

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.SpotiFlyerException
import org.koin.dsl.bind
import org.koin.dsl.module

class AndroidMediaConverter : MediaConverter() {
    override suspend fun convertAudioFile(
        inputFilePath: String,
        outputFilePath: String,
        audioQuality: AudioQuality,
        progressCallbacks: (Long) -> Unit,
    ) = executeSafelyInPool {
        val kbpsArg = if (audioQuality == AudioQuality.UNKNOWN) "" else "-b:a ${audioQuality.kbps}k"

        val session = FFmpegKit.execute(
            "-i $inputFilePath -y $kbpsArg -acodec libmp3lame -vn $outputFilePath"
        )

        when (session.returnCode.value) {
            ReturnCode.SUCCESS -> {
                //FFMPEG task Completed
                outputFilePath
            }
            ReturnCode.CANCEL -> {
                throw SpotiFlyerException.MP3ConversionFailed("FFmpeg Conversion Canceled for $inputFilePath")
            }
            else -> throw SpotiFlyerException.MP3ConversionFailed("FFmpeg Conversion Failed for $inputFilePath")
        }
    }

}

internal actual fun mediaConverterModule() = module {
    single { AndroidMediaConverter() } bind MediaConverter::class
}