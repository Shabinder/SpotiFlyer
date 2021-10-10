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

package com.shabinder.common.core_components.file_manager

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import co.touchlab.kermit.Kermit
import com.github.kokorin.jaffree.JaffreeException
import com.mpatric.mp3agic.InvalidDataException
import com.mpatric.mp3agic.Mp3File
import com.shabinder.common.core_components.media_converter.MediaConverter
import com.shabinder.common.core_components.parallel_executor.ParallelExecutor
import com.shabinder.common.core_components.picture.Picture
import com.shabinder.common.core_components.preference_manager.PreferenceManager
import com.shabinder.common.core_components.removeAllTags
import com.shabinder.common.core_components.setId3v1Tags
import com.shabinder.common.core_components.setId3v2TagsAndSaveFile
import com.shabinder.common.database.SpotiFlyerDatabase
import com.shabinder.common.models.Actions
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.dispatcherIO
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import com.shabinder.common.models.event.coroutines.map
import com.shabinder.database.Database
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import org.koin.dsl.bind
import org.koin.dsl.module
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO

internal actual fun fileManagerModule() = module {
    single { DesktopFileManager(get(), get(), get(), get()) } bind FileManager::class
}

val DownloadProgressFlow: MutableSharedFlow<HashMap<String, DownloadStatus>> = MutableSharedFlow(1)

// Scope Allowing 4 Parallel Downloads
val DownloadScope = ParallelExecutor(Dispatchers.IO)

class DesktopFileManager(
    override val logger: Kermit,
    override val preferenceManager: PreferenceManager,
    override val mediaConverter: MediaConverter,
    spotiFlyerDatabase: SpotiFlyerDatabase,
) : FileManager {

    init {
        createDirectories()
    }

    override fun fileSeparator(): String = File.separator

    override fun imageCacheDir(): String = System.getProperty("user.home") +
            fileSeparator() + "SpotiFlyer/.images" + fileSeparator()

    private val defaultBaseDir = System.getProperty("user.home")

    override fun defaultDir(): String = (preferenceManager.downloadDir ?: defaultBaseDir) + fileSeparator() +
            "SpotiFlyer" + fileSeparator()

    override fun isPresent(path: String): Boolean = File(path).exists()

    override fun createDirectory(dirPath: String) {
        val yourAppDir = File(dirPath)

        if (!yourAppDir.exists() && !yourAppDir.isDirectory) { // create empty directory
            if (yourAppDir.mkdirs()) {
                logger.i { "$dirPath created" }
            } else {
                logger.e { "Unable to create Dir: $dirPath!" }
            }
        } else {
            logger.i { "$dirPath already exists" }
        }
    }

    override suspend fun clearCache() {
        File(imageCacheDir()).deleteRecursively()
    }

    override suspend fun cacheImage(image: Any, path: String): Unit = withContext(dispatcherIO) {
        try {
            val file = File(path)
            if(!file.parentFile.exists()) createDirectories()
            (image as? BufferedImage)?.let {
                ImageIO.write(it, "jpeg", file)
             }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun saveFileWithMetadata(
        mp3ByteArray: ByteArray,
        trackDetails: TrackDetails,
        postProcess: (track: TrackDetails) -> Unit
    ) = withContext(dispatcherIO) {
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
            try {
                // Add Mp3 Tags and Add to Library
                Mp3File(File(songFile.absolutePath))
                    .removeAllTags()
                    .setId3v1Tags(trackDetails)
                    .setId3v2TagsAndSaveFile(trackDetails)
                addToLibrary(songFile.absolutePath)
            } catch (e: Exception) {
                // Media File Isn't MP3 lets Convert It first
                if (e is InvalidDataException) {
                    val convertedFilePath = songFile.absolutePath.substringBeforeLast('.') + ".temp.mp3"

                    val conversionResult = mediaConverter.convertAudioFile(
                        inputFilePath = songFile.absolutePath,
                        outputFilePath = convertedFilePath,
                        trackDetails.audioQuality
                    )

                    conversionResult.map { outputFilePath ->
                        Mp3File(File(outputFilePath))
                            .removeAllTags()
                            .setId3v1Tags(trackDetails)
                            .setId3v2TagsAndSaveFile(trackDetails, trackDetails.outputFilePath)

                        addToLibrary(trackDetails.outputFilePath)
                    }.fold(
                        success = {},
                        failure = {
                            throw it
                        }
                    )
                    File(convertedFilePath).delete()
                } else throw e
            }
            SuspendableEvent.success(trackDetails.outputFilePath)
        } catch (e: Throwable) {
            if (e is JaffreeException) Actions.instance.showPopUpMessage("No FFmpeg found at path.")
            if (songFile.exists()) songFile.delete()
            logger.e { "${songFile.absolutePath} could not be created" }
            SuspendableEvent.error(e)
        }
    }

    override fun addToLibrary(path: String) {}

    override suspend fun loadImage(url: String, reqWidth: Int, reqHeight: Int): Picture {
        var picture: ImageBitmap? = loadCachedImage(getImageCachePath(url), reqWidth, reqHeight)
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

    @OptIn(DelicateCoroutinesApi::class)
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
                        cacheImage(result, getImageCachePath(url))
                    }
                    result.toImageBitmap()
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override val db: Database? = spotiFlyerDatabase.instance
}

fun BufferedImage.toImageBitmap() = Image.makeFromEncoded(
    toByteArray(this)
).asImageBitmap()

private fun toByteArray(bitmap: BufferedImage): ByteArray {
    val baOs = ByteArrayOutputStream()
    ImageIO.write(bitmap, "png", baOs)
    return baOs.toByteArray()
}
