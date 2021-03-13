package com.shabinder.common.di

import co.touchlab.kermit.Kermit
import com.shabinder.common.models.DownloadResult
import com.shabinder.common.models.TrackDetails
import com.shabinder.database.Database
import kotlinext.js.Object
import kotlinext.js.asJsObject
import kotlinext.js.js
import kotlinext.js.jsObject
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.khronos.webgl.ArrayBuffer
import org.w3c.dom.ImageBitmap
import org.khronos.webgl.Int8Array

actual class Dir actual constructor(
    private val logger: Kermit,
    private val database: Database?,
) {

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

    actual fun createDirectory(dirPath:String){}

    actual suspend fun clearCache() {}

    actual suspend fun cacheImage(image: Any,path:String) {}

    @Suppress("BlockingMethodInNonBlockingContext")
    actual suspend  fun saveFileWithMetadata(
            mp3ByteArray: ByteArray,
            trackDetails: TrackDetails
    ) {
        val writer = ID3Writer(mp3ByteArray.toArrayBuffer())
        val albumArt = downloadFile(trackDetails.albumArtURL)
        albumArt.collect {
            when(it){
                is DownloadResult.Success -> {
                    println("Album Art Downloaded Success")
                    val albumArtObj = js {
                        this["type"] = 3
                        this["data"] = it.byteArray.toArrayBuffer()
                        this["description"] = "Cover Art"
                    }
                    writeTagsAndSave(writer, albumArtObj as Object,trackDetails)
                }
                is DownloadResult.Error -> {
                    println("Album Art Downloading Error")
                    writeTagsAndSave(writer,null,trackDetails)
                }
                is DownloadResult.Progress -> println("Album Art Downloading: ${it.progress}")
            }
        }
    }

    private suspend fun writeTagsAndSave(writer:ID3Writer, albumArt:Object?, trackDetails: TrackDetails){
        writer.apply {
            setFrame("TIT2", trackDetails.title)
            setFrame("TPE1", trackDetails.artists.toTypedArray())
            setFrame("TALB", trackDetails.albumName?:"")
            try{trackDetails.year?.substring(0,4)?.toInt()?.let { setFrame("TYER", it) }} catch(e:Exception){}
            setFrame("TPE2", trackDetails.artists.joinToString(","))
            setFrame("WOAS", trackDetails.source.toString())
            setFrame("TLEN", trackDetails.durationSec)
            albumArt?.let { setFrame("APIC", it) }
        }
        writer.addTag()
        saveAs(writer.getBlob(), "${removeIllegalChars(trackDetails.title)}.mp3")
    }

    actual fun addToLibrary(path:String){}

    actual suspend fun loadImage(url: String): Picture {
        return Picture(url)
    }

    private fun loadCachedImage(cachePath: String): ImageBitmap? = null

    private suspend fun freshImage(url:String): ImageBitmap? = null

    actual val db: Database?
        get() = database
}

fun ByteArray.toArrayBuffer():ArrayBuffer{
    return this.unsafeCast<Int8Array>().buffer
}