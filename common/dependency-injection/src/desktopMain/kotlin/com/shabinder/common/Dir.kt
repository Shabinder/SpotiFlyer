package com.shabinder.common

import co.touchlab.kermit.Kermit
import com.mpatric.mp3agic.Mp3File
import java.io.*
import java.nio.charset.StandardCharsets
import javax.imageio.ImageIO

actual open class Dir actual constructor(private val logger: Kermit) {

    actual fun fileSeparator(): String = File.separator

    actual fun imageCacheDir(): String = System.getProperty("user.home") +
            fileSeparator() + "SpotiFlyer/.images" + fileSeparator()

    actual fun defaultDir(): String = System.getProperty("user.home") + fileSeparator() +
            "SpotiFlyer" + fileSeparator()

    actual fun isPresent(path: String): Boolean = File(path).exists()

    actual fun createDirectory(dirPath:String){
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

    actual suspend fun clearCache() {
        File(imageCacheDir()).deleteRecursively()
    }

    actual fun cacheImage(picture: Picture) {
        try {
            val path = imageCacheDir() + picture.name

            ImageIO.write(picture.image, "jpeg", File(path))

            val bw =
                BufferedWriter(
                    OutputStreamWriter(
                        FileOutputStream(path + cacheImagePostfix()),
                        StandardCharsets.UTF_8
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
    actual suspend  fun saveFileWithMetadata(
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
