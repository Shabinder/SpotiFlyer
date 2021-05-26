/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.common.di

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import co.touchlab.kermit.Kermit
import com.mpatric.mp3agic.Mp3File
import com.russhwolf.settings.Settings
import com.shabinder.common.database.SpotiFlyerDatabase
import com.shabinder.common.models.TrackDetails
import com.shabinder.database.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.skija.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO

actual class Dir actual constructor(
    private val logger: Kermit,
    settingsPref: Settings,
    spotiFlyerDatabase: SpotiFlyerDatabase,
) {

    init {
        createDirectories()
    }

    actual fun fileSeparator(): String = File.separator

    actual fun imageCacheDir(): String = System.getProperty("user.home") +
        fileSeparator() + "SpotiFlyer/.images" + fileSeparator()

    private val defaultBaseDir = System.getProperty("user.home")!!

    actual fun defaultDir(): String = (settings.getStringOrNull(DirKey) ?: defaultBaseDir) + fileSeparator() +
        "SpotiFlyer" + fileSeparator()

    actual fun isPresent(path: String): Boolean = File(path).exists()

    actual fun createDirectory(dirPath: String) {
        val yourAppDir = File(dirPath)

        if (!yourAppDir.exists() && !yourAppDir.isDirectory) { // create empty directory
            if (yourAppDir.mkdirs()) { logger.i { "$dirPath created" } } else {
                logger.e { "Unable to create Dir: $dirPath!" }
            }
        } else {
            logger.i { "$dirPath already exists" }
        }
    }

    actual suspend fun clearCache() {
        File(imageCacheDir()).deleteRecursively()
    }

    actual suspend fun cacheImage(image: Any, path: String) {
        try {
            (image as? BufferedImage)?.let {
                ImageIO.write(it, "jpeg", File(path))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    actual suspend fun saveFileWithMetadata(
        mp3ByteArray: ByteArray,
        trackDetails: TrackDetails,
        postProcess: (track: TrackDetails) -> Unit
    ) {
        val songFile = File(trackDetails.outputFilePath)
        try {
            /*
            * Check , if Fetch was Used, File is saved Already, else write byteArray we Received
            * */
            if (!songFile.exists()) {
                /*Make intermediate Dirs if they don't exist yet*/
                songFile.parentFile.mkdirs()
            }

            if (mp3ByteArray.isNotEmpty()) songFile.writeBytes(mp3ByteArray)

            when (trackDetails.outputFilePath.substringAfterLast('.')) {
                ".mp3" -> {
                    Mp3File(File(songFile.absolutePath))
                        .removeAllTags()
                        .setId3v1Tags(trackDetails)
                        .setId3v2TagsAndSaveFile(trackDetails)
                    addToLibrary(songFile.absolutePath)
                }
                ".m4a" -> {
                    /*FFmpeg.executeAsync(
                        "-i ${m4aFile.absolutePath} -y -b:a 160k -acodec libmp3lame -vn ${m4aFile.absolutePath.substringBeforeLast('.') + ".mp3"}"
                    ){ _, returnCode ->
                        when (returnCode) {
                            Config.RETURN_CODE_SUCCESS  -> {
                                //FFMPEG task Completed
                                logger.d{ "Async command execution completed successfully." }
                                scope.launch {
                                    Mp3File(File(m4aFile.absolutePath.substringBeforeLast('.') + ".mp3"))
                                        .removeAllTags()
                                        .setId3v1Tags(trackDetails)
                                        .setId3v2TagsAndSaveFile(trackDetails)
                                    addToLibrary(m4aFile.absolutePath.substringBeforeLast('.') + ".mp3")
                                }
                            }
                            Config.RETURN_CODE_CANCEL -> {
                                logger.d{"Async command execution cancelled by user."}
                            }
                            else -> {
                                logger.d { "Async command execution failed with rc=$returnCode" }
                            }
                        }
                    }*/
                }
                else -> {
                    try {
                        Mp3File(File(songFile.absolutePath))
                            .removeAllTags()
                            .setId3v1Tags(trackDetails)
                            .setId3v2TagsAndSaveFile(trackDetails)
                        addToLibrary(songFile.absolutePath)
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                // Toast.makeText(appContext,"Could Not Create File:\n${songFile.absolutePath}",Toast.LENGTH_SHORT).show()
            }
            if (songFile.exists()) songFile.delete()
            logger.e { "${songFile.absolutePath} could not be created" }
        }
    }
    actual fun addToLibrary(path: String) {}

    actual suspend fun loadImage(url: String, reqWidth: Int, reqHeight: Int): Picture {
        val cachePath = imageCacheDir() + getNameURL(url)
        var picture: ImageBitmap? = loadCachedImage(cachePath, reqWidth, reqHeight)
        if (picture == null) picture = freshImage(url, reqWidth, reqHeight)
        return Picture(image = picture)
    }

    private fun loadCachedImage(cachePath: String, reqWidth: Int, reqHeight: Int): ImageBitmap? {
        return try {
            ImageIO.read(File(cachePath))?.toImageBitmap()
        } catch (e: Exception) {
            // e.printStackTrace()
            null
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun freshImage(url: String, reqWidth: Int, reqHeight: Int): ImageBitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val source = URL(url)
                val connection: HttpURLConnection = source.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.connect()

                val input: InputStream = connection.inputStream
                val result: BufferedImage? = ImageIO.read(input)

                if (result != null) {
                    GlobalScope.launch(Dispatchers.IO) { // TODO Refactor
                        cacheImage(result, imageCacheDir() + getNameURL(url))
                    }
                    result.toImageBitmap()
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    actual val db: Database? = spotiFlyerDatabase.instance
    actual val settings: Settings = settingsPref
}

fun BufferedImage.toImageBitmap() = Image.makeFromEncoded(
    toByteArray(this)
).asImageBitmap()

private fun toByteArray(bitmap: BufferedImage): ByteArray {
    val baOs = ByteArrayOutputStream()
    ImageIO.write(bitmap, "png", baOs)
    return baOs.toByteArray()
}
