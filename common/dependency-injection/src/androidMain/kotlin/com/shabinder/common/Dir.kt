package com.shabinder.common

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import co.touchlab.kermit.Kermit
import com.mpatric.mp3agic.Mp3File
import com.shabinder.common.database.appContext
import java.io.*
import java.nio.charset.StandardCharsets

actual open class Dir actual constructor(
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

    actual fun cacheImage(picture: Picture) {
        try {
            val path = imageCacheDir() + picture.name
            FileOutputStream(path).use { out ->
                picture.image.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            val bw =
                BufferedWriter(
                    OutputStreamWriter(
                        FileOutputStream(path + cacheImagePostfix()), StandardCharsets.UTF_8
                    )
                )

            bw.write(picture.source)
            bw.write("\r\n${picture.width}")
            bw.write("\r\n${picture.height}")
            bw.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    actual suspend fun saveFileWithMetadata(
        mp3ByteArray: ByteArray,
        path: String,
        trackDetails: TrackDetails
    ) {
        val file = File(path)
        file.writeBytes(mp3ByteArray)

        Mp3File(file)
            .removeAllTags()
            .setId3v1Tags(trackDetails)
            .setId3v2TagsAndSaveFile(trackDetails,path)
    }
}