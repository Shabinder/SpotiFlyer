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

import co.touchlab.kermit.Kermit
import com.shabinder.common.core_components.ID3Writer
import com.shabinder.common.core_components.media_converter.MediaConverter
import com.shabinder.common.core_components.picture.Picture
import com.shabinder.common.core_components.preference_manager.PreferenceManager
import com.shabinder.common.core_components.saveAs
import com.shabinder.common.database.SpotiFlyerDatabase
import com.shabinder.common.models.DownloadResult
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.corsApi
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import com.shabinder.common.utils.removeIllegalChars
import com.shabinder.database.Database
import kotlinext.js.Object
import kotlinext.js.js
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.koin.dsl.bind
import org.koin.dsl.module
import org.w3c.dom.ImageBitmap


internal actual fun fileManagerModule() = module {
    single { WebFileManager(get(), get(), get(), get()) } bind FileManager::class
}

class WebFileManager(
    override val logger: Kermit,
    override val preferenceManager: PreferenceManager,
    override val mediaConverter: MediaConverter,
    spotiFlyerDatabase: SpotiFlyerDatabase,
) : FileManager {
    /*init {
        createDirectories()
    }*/

    /*
    * TODO
    * */
    override fun fileSeparator(): String = "/"

    override fun imageCacheDir(): String = "TODO" +
            fileSeparator() + "SpotiFlyer/.images" + fileSeparator()

    override fun defaultDir(): String = "TODO" + fileSeparator() +
            "SpotiFlyer" + fileSeparator()

    override fun isPresent(path: String): Boolean = false

    override fun createDirectory(dirPath: String) {}

    override suspend fun clearCache() {}

    override suspend fun cacheImage(image: Any, path: String) {}

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun saveFileWithMetadata(
        mp3ByteArray: ByteArray,
        trackDetails: TrackDetails,
        postProcess: (track: TrackDetails) -> Unit
    ): SuspendableEvent<String, Throwable> {
        return SuspendableEvent {
            val writer = ID3Writer(mp3ByteArray.toArrayBuffer())
            val albumArt = downloadFile(corsApi + trackDetails.albumArtURL)
            albumArt.collect {
                when (it) {
                    is DownloadResult.Success -> {
                        logger.d { "Album Art Downloaded Success" }
                        val albumArtObj = js {
                            this["type"] = 3
                            this["data"] = it.byteArray.toArrayBuffer()
                            this["description"] = "Cover Art"
                        }
                        writeTagsAndSave(writer, albumArtObj as Object, trackDetails)
                    }
                    is DownloadResult.Error -> {
                        logger.d { "Album Art Downloading Error" }
                        writeTagsAndSave(writer, null, trackDetails)
                    }
                    is DownloadResult.Progress -> logger.d { "Album Art Downloading: ${it.progress}" }
                }
            }
            trackDetails.outputFilePath
        }
    }

    private suspend fun writeTagsAndSave(writer: ID3Writer, albumArt: Object?, trackDetails: TrackDetails) {
        writer.apply {
            setFrame("TIT2", trackDetails.title)
            setFrame("TPE1", trackDetails.artists.toTypedArray())
            setFrame("TALB", trackDetails.albumName ?: "")
            try {
                trackDetails.year?.substring(0, 4)?.toInt()?.let { setFrame("TYER", it) }
            } catch (e: Exception) {
            }
            setFrame("TPE2", trackDetails.artists.joinToString(","))
            setFrame("WOAS", trackDetails.source.toString())
            setFrame("TLEN", trackDetails.durationSec)
            albumArt?.let { setFrame("APIC", it) }
        }
        writer.addTag()
        allTracksStatus[trackDetails.title] = DownloadStatus.Downloaded
        DownloadProgressFlow.emit(allTracksStatus)
        saveAs(writer.getBlob(), "${removeIllegalChars(trackDetails.title)}.mp3")
    }

    override fun addToLibrary(path: String) {}

    override suspend fun loadImage(url: String, reqWidth: Int, reqHeight: Int): Picture {
        return Picture(url)
    }

    private fun loadCachedImage(cachePath: String): ImageBitmap? = null

    private suspend fun freshImage(url: String): ImageBitmap? = null

    override val db: Database? = spotiFlyerDatabase.instance
}

fun ByteArray.toArrayBuffer(): ArrayBuffer {
    return this.unsafeCast<Int8Array>().buffer
}

val DownloadProgressFlow: MutableSharedFlow<HashMap<String, DownloadStatus>> = MutableSharedFlow(1)

// Error:https://github.com/Kotlin/kotlinx.atomicfu/issues/182
// val DownloadScope = ParallelExecutor(Dispatchers.Default) //Download Pool of 4 parallel
val allTracksStatus: HashMap<String, DownloadStatus> = hashMapOf()
