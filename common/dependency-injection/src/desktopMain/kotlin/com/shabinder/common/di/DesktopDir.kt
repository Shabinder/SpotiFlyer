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

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import co.touchlab.kermit.Kermit
import com.mpatric.mp3agic.Mp3File
import com.shabinder.common.models.TrackDetails
import com.shabinder.database.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.skija.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO




actual class Dir actual constructor(
    private val logger: Kermit,
    private val database: Database?,
    ) {

    init {
        createDirectories()
    }

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
        File(imageCacheDir()).  deleteRecursively()
    }

    actual suspend fun cacheImage(image: Any,path:String) {
        try {
            (image as? BufferedImage)?.let {
                ImageIO.write(it,"jpeg", File(path))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    actual suspend  fun saveFileWithMetadata(
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
    actual fun addToLibrary(path:String){}

    actual suspend fun loadImage(url: String): Picture {
        val cachePath = imageCacheDir() + getNameURL(url)
        var picture: ImageBitmap? = loadCachedImage(cachePath)
        if (picture == null) picture = freshImage(url)
        return Picture(image = picture)
    }

    private fun loadCachedImage(cachePath: String): ImageBitmap? {
        return try {
            ImageIO.read(File(cachePath))?.toImageBitmap()
        } catch (e: Exception) {
            //e.printStackTrace()
            null
        }
    }

    private suspend fun freshImage(url:String): ImageBitmap?{
        return try {
            val source = URL(url)
            val connection: HttpURLConnection = source.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.connect()

            val input: InputStream = connection.inputStream
            val result: BufferedImage? = ImageIO.read(input)

            if (result != null) {
                GlobalScope.launch(Dispatchers.IO) { //TODO Refactor
                    cacheImage(result,imageCacheDir() + getNameURL(url))
                }
                result.toImageBitmap()
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual val db: Database?
        get() = database
}
fun BufferedImage.toImageBitmap() = Image.makeFromEncoded(
    toByteArray(this)
).asImageBitmap()

private fun toByteArray(bitmap: BufferedImage) : ByteArray {
    val baOs = ByteArrayOutputStream()
    ImageIO.write(bitmap, "png", baOs)
    return baOs.toByteArray()
}
