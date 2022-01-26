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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.compose.ui.graphics.asImageBitmap
import co.touchlab.kermit.Kermit
import com.mpatric.mp3agic.InvalidDataException
import com.mpatric.mp3agic.Mp3File
import com.shabinder.common.core_components.media_converter.MediaConverter
import com.shabinder.common.core_components.media_converter.removeAllTags
import com.shabinder.common.core_components.media_converter.setId3v1Tags
import com.shabinder.common.core_components.media_converter.setId3v2TagsAndSaveFile
import com.shabinder.common.core_components.parallel_executor.ParallelExecutor
import com.shabinder.common.core_components.picture.Picture
import com.shabinder.common.core_components.preference_manager.PreferenceManager
import com.shabinder.common.database.SpotiFlyerDatabase
import com.shabinder.common.di.getMemoryEfficientBitmap
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.dispatcherIO
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import com.shabinder.common.models.event.coroutines.map
import com.shabinder.common.models.Actions
import com.shabinder.common.models.AudioFormat
import com.shabinder.database.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.dsl.bind
import org.koin.dsl.module
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

internal actual fun fileManagerModule() = module {
    single { AndroidFileManager(get(), get(), get(), get()) } bind FileManager::class
}

/*
* Ignore Deprecation
*  `Deprecation is only a Suggestion P->`
* */
@Suppress("DEPRECATION")
class AndroidFileManager(
    override val logger: Kermit,
    override val preferenceManager: PreferenceManager,
    override val mediaConverter: MediaConverter,
    spotiFlyerDatabase: SpotiFlyerDatabase
) : FileManager {
    @Suppress("DEPRECATION")
    private val defaultBaseDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString()

    override fun fileSeparator(): String = File.separator

    override fun imageCacheDir(): String = Actions.instance.platformActions.imageCacheDir

    // fun call in order to always access Updated Value
    override fun defaultDir(): String = (preferenceManager.downloadDir ?: defaultBaseDir) +
            File.separator + "SpotiFlyer" + File.separator

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

    @Suppress("unused")
    override suspend fun clearCache(): Unit = withContext(dispatcherIO) {
        File(imageCacheDir()).deleteRecursively()
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
                songFile.parentFile?.mkdirs()
            }
            // Write Bytes to Media File
            songFile.writeBytes(mp3ByteArray)

            try {
                // Add Mp3 Tags and Add to Library
                if (trackDetails.audioFormat != AudioFormat.MP3)
                    throw InvalidDataException("Audio Format is ${trackDetails.audioFormat}, Needs Conversion!")

                Mp3File(File(songFile.absolutePath))
                    .removeAllTags()
                    .setId3v1Tags(trackDetails)
                    .setId3v2TagsAndSaveFile(trackDetails)
                addToLibrary(songFile.absolutePath)
            } catch (e: Exception) {
                // Media File Isn't MP3 lets Convert It first
                if (e is InvalidDataException) {
                    val convertedFilePath =
                        songFile.absolutePath.substringBeforeLast('.') + ".temp.mp3"

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
            e.printStackTrace()
            if (songFile.exists()) songFile.delete()
            logger.e { "${songFile.absolutePath} could not be created" }
            SuspendableEvent.error(e)
        }
    }

    override fun addToLibrary(path: String) = Actions.instance.platformActions.addToLibrary(path)

    override suspend fun loadImage(url: String, reqWidth: Int, reqHeight: Int): Picture =
        withContext(dispatcherIO) {
            val cachePath = getImageCachePath(url)
            Picture(
                image = (loadCachedImage(cachePath, reqWidth, reqHeight) ?: freshImage(
                    url,
                    reqWidth,
                    reqHeight
                ))?.asImageBitmap()
            )
        }

    private fun loadCachedImage(cachePath: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            getMemoryEfficientBitmap(cachePath, reqWidth, reqHeight)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun cacheImage(image: Any, path: String): Unit = withContext(dispatcherIO) {
        try {
            FileOutputStream(path).use { out ->
                (image as? Bitmap)?.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun freshImage(url: String, reqWidth: Int, reqHeight: Int): Bitmap? =
        withContext(dispatcherIO) {
            try {
                val source = URL(url)
                val connection: HttpURLConnection = source.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.connect()

                val input: ByteArray = connection.inputStream.readBytes()

                // Get Memory Efficient Bitmap
                val bitmap: Bitmap? = getMemoryEfficientBitmap(input, reqWidth, reqHeight)

                parallelExecutor.execute {
                    // Decode and Cache Full Sized Image in Background
                    cacheImage(
                        BitmapFactory.decodeByteArray(input, 0, input.size),
                        getImageCachePath(url)
                    )
                }
                bitmap // return Memory Efficient Bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    /*
    * Parallel Executor with 2 concurrent operation at a time.
    *   -   We will use this to queue up operations and decode Full Sized Images
    *   -   Will Decode Only a small set of images at a time , to avoid going into `Out of Memory`
    * */
    private val parallelExecutor = ParallelExecutor(Dispatchers.IO, 2)

    override val db: Database? = spotiFlyerDatabase.instance
}
