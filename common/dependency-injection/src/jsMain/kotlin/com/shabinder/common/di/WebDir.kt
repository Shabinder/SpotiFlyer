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

import co.touchlab.kermit.Kermit
import com.russhwolf.settings.Settings
import com.shabinder.common.database.SpotiFlyerDatabase
import com.shabinder.common.di.gaana.corsApi
import com.shabinder.common.di.utils.removeIllegalChars
import com.shabinder.common.models.DownloadResult
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import com.shabinder.database.Database
import kotlinext.js.Object
import kotlinext.js.js
import kotlinx.coroutines.flow.collect
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.ImageBitmap

actual class Dir actual constructor(
    private val logger: Kermit,
    private val settings: Settings,
    private val spotiFlyerDatabase: SpotiFlyerDatabase,
) {
    companion object {
        const val DirKey = "downloadDir"
        const val AnalyticsKey = "analytics"
    }

    actual val isAnalyticsEnabled get() = settings.getBooleanOrNull(AnalyticsKey) ?: false

    actual fun enableAnalytics() {
        settings.putBoolean(AnalyticsKey,true)
    }

    actual fun setDownloadDirectory(newBasePath:String) = settings.putString(DirKey,newBasePath)

    /*init {
        createDirectories()
    }*/

    /*
    * TODO
    * */
    actual fun fileSeparator(): String = "/"

    actual fun imageCacheDir(): String = "TODO" +
        fileSeparator() + "SpotiFlyer/.images" + fileSeparator()

    actual fun defaultDir(): String = "TODO" + fileSeparator() +
        "SpotiFlyer" + fileSeparator()

    actual fun isPresent(path: String): Boolean = false

    actual fun createDirectory(dirPath: String) {}

    actual suspend fun clearCache() {}

    actual suspend fun cacheImage(image: Any, path: String) {}

    @Suppress("BlockingMethodInNonBlockingContext")
    actual suspend fun saveFileWithMetadata(
        mp3ByteArray: ByteArray,
        trackDetails: TrackDetails,
        postProcess:(track: TrackDetails)->Unit
    ) {
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
    }

    private suspend fun writeTagsAndSave(writer: ID3Writer, albumArt: Object?, trackDetails: TrackDetails) {
        writer.apply {
            setFrame("TIT2", trackDetails.title)
            setFrame("TPE1", trackDetails.artists.toTypedArray())
            setFrame("TALB", trackDetails.albumName ?: "")
            try { trackDetails.year?.substring(0, 4)?.toInt()?.let { setFrame("TYER", it) } } catch (e: Exception) {}
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

    actual fun addToLibrary(path: String) {}

    actual suspend fun loadImage(url: String): Picture {
        return Picture(url)
    }

    private fun loadCachedImage(cachePath: String): ImageBitmap? = null

    private suspend fun freshImage(url: String): ImageBitmap? = null

    actual val db: Database? get() = spotiFlyerDatabase.instance
}

fun ByteArray.toArrayBuffer(): ArrayBuffer {
    return this.unsafeCast<Int8Array>().buffer
}
