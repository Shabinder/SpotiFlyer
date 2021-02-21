package com.shabinder.common.di

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import co.touchlab.kermit.Kermit
import com.mpatric.mp3agic.Mp3File
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.database.appContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

actual class Dir actual constructor(
    private val logger: Kermit
) {

    private val context: Context
        get() = appContext

    actual fun fileSeparator(): String = File.separator

    actual fun imageCacheDir(): String = context.cacheDir.absolutePath + File.separator

    @Suppress("DEPRECATION")
    actual fun defaultDir(): String =
        Environment.getExternalStorageDirectory().toString() + File.separator +
                Environment.DIRECTORY_MUSIC + File.separator +
                "SpotiFlyer"+ File.separator

    actual fun isPresent(path: String): Boolean = File(path).exists()

    actual fun createDirectory(dirPath: String) {
        val yourAppDir = File(dirPath)

        if(!yourAppDir.exists() && !yourAppDir.isDirectory)
        { // create empty directory
            if (yourAppDir.mkdirs())
            {logger.i{"$dirPath created"}}
            else
            {
                logger.e{"Unable to create Dir: $dirPath!"}
            }
        }
        else {
            logger.i { "$dirPath already exists" }
        }
    }

    actual suspend fun clearCache(){
        File(imageCacheDir()).deleteRecursively()
    }

    actual suspend fun cacheImage(image: Any,path:String) {
        try {
            FileOutputStream(path).use { out ->
                (image as? Bitmap)?.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    actual suspend fun saveFileWithMetadata(
        mp3ByteArray: ByteArray,
        trackDetails: TrackDetails
    ) {
        val file = File(trackDetails.outputFilePath)
        file.writeBytes(mp3ByteArray)

        Mp3File(file)
            .removeAllTags()
            .setId3v1Tags(trackDetails)
            .setId3v2TagsAndSaveFile(trackDetails)
    }

    actual suspend fun loadImage(url: String): ImageBitmap? {
        val cachePath = imageCacheDir() + getNameURL(url)
        return (loadCachedImage(cachePath) ?: freshImage(url))?.asImageBitmap()
    }

    private fun loadCachedImage(cachePath: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(cachePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private suspend fun freshImage(url:String): Bitmap?{
        return try {
            val source = URL(url)
            val connection: HttpURLConnection = source.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.connect()

            val input: InputStream = connection.inputStream
            val result: Bitmap? = BitmapFactory.decodeStream(input)

            if (result != null) {
                GlobalScope.launch(Dispatchers.IO) {
                    cacheImage(result,imageCacheDir() + getNameURL(url))
                }
                result
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}