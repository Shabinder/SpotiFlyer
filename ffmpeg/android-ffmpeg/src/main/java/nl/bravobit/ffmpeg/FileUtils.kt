package nl.bravobit.ffmpeg

import android.content.Context
import java.io.File

internal object FileUtils {

    private const val FFMPEG_FILE_NAME = "lib..ffmpeg..so"
    private const val FFPROBE_FILE_NAME = "lib..ffprobe..so"

    @JvmStatic
    fun getFFmpeg(context: Context): File {
        val folder = File(context.applicationInfo.nativeLibraryDir)
        return File(folder, FFMPEG_FILE_NAME)
    }

    @JvmStatic
    fun getFFprobe(context: Context): File {
        val folder = File(context.applicationInfo.nativeLibraryDir)
        return File(folder, FFPROBE_FILE_NAME)
    }
}