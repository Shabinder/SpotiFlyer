package com.shabinder.common.di

import co.touchlab.kermit.Kermit
import com.mpatric.mp3agic.Mp3File
import com.shabinder.common.models.TrackDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.image.BufferedImage
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.imageio.ImageIO

actual class Dir actual constructor(private val logger: Kermit) {

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

    actual suspend fun cacheImage(picture: Picture) {
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

    actual fun loadImage(url: String): Picture? {
        val cachePath = imageCacheDir() + getNameURL(url)
        var picture: Picture? = loadCachedImage(cachePath)
        if (picture == null) picture = freshImage(url,cachePath)
        return picture
    }

    private fun loadCachedImage(cachePath: String): Picture? {
        return try {
            val read = BufferedReader(
                InputStreamReader(
                    FileInputStream(cachePath + cacheImagePostfix()),
                    StandardCharsets.UTF_8
                )
            )

            val source = read.readLine()
            val width = read.readLine().toInt()
            val height = read.readLine().toInt()

            read.close()

            val result: BufferedImage? = ImageIO.read(File(cachePath))

            if (result != null) {
                com.shabinder.common.di.Picture(
                    source,
                    getNameURL(source),
                    result,
                    width,
                    height
                )
            }else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun freshImage(url:String,cachePath: String): Picture?{
        return try {
            val source = URL(url)
            val connection: HttpURLConnection = source.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.connect()

            val input: InputStream = connection.inputStream
            val result: BufferedImage? = ImageIO.read(input)

            if (result != null) {
                val picture = com.shabinder.common.di.Picture(
                    url,
                    getNameURL(url),
                    result,
                    result.width,
                    result.height
                )
                GlobalScope.launch(Dispatchers.IO) { //TODO Refactor
                    cacheImage(picture)
                }
                picture
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
