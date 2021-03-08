package com.shabinder.common.di

import co.touchlab.kermit.Kermit
import com.shabinder.common.models.TrackDetails
import com.shabinder.database.Database
import org.w3c.dom.ImageBitmap

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

    actual fun createDirectory(dirPath:String){

    }

    actual suspend fun clearCache() {
    }

    actual suspend fun cacheImage(image: Any,path:String) {}

    @Suppress("BlockingMethodInNonBlockingContext")
    actual suspend  fun saveFileWithMetadata(
            mp3ByteArray: ByteArray,
            trackDetails: TrackDetails
    ) {
    }

    actual fun addToLibrary(path:String){}

    actual suspend fun loadImage(url: String): Picture {
        return Picture(url)
    }

    private fun loadCachedImage(cachePath: String): ImageBitmap? {
        return null
    }

    private suspend fun freshImage(url:String): ImageBitmap?{
        return null
    }

    actual val db: Database?
        get() = database
}
