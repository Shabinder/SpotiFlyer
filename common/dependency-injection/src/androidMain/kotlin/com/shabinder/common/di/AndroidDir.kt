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

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.asImageBitmap
import co.touchlab.kermit.Kermit
import com.github.k1rakishou.fsaf.FileManager
import com.github.k1rakishou.fsaf.file.DirectorySegment
import com.github.k1rakishou.fsaf.file.FileSegment
import com.github.k1rakishou.fsaf.manager.base_directory.BaseDirectory
import com.mpatric.mp3agic.Mp3File
import com.russhwolf.settings.Settings
import com.shabinder.common.database.SpotiFlyerDatabase
import com.shabinder.common.di.utils.removeIllegalChars
import com.shabinder.common.models.File
import com.shabinder.common.models.SpotiFlyerBaseDir
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.methods
import com.shabinder.database.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


/*
* Ignore Deprecation
*  Deprecation is only a Suggestion P-)
* */
@Suppress("DEPRECATION")
actual class Dir actual constructor(
    private val logger: Kermit,
    private val settings: Settings,
    spotiFlyerDatabase: SpotiFlyerDatabase,
): KoinComponent {

    private val context: Context = get()
    val fileManager = FileManager(context)

    init {
        fileManager.apply {
            registerBaseDir<SpotiFlyerBaseDir>(SpotiFlyerBaseDir({ getDirType() },
                getJavaFile = {
                    java.io.File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                            .toString()
                                + "/SpotiFlyer/"
                    )
                },
                getSAFUri = {
                    settings.getStringOrNull(DirKey)?.let {
                        Uri.parse(it)
                    }
                }
            ))
            defaultDir().documentFile?.let {
                createSnapshot(it,true)
            }
        }
    }

    companion object {
        const val DirKey = "downloadDir"
        const val AnalyticsKey = "analytics"
    }

    /*
    * Do we have Analytics Permission?
    *   -   Defaults to `False`
    * */
    actual val isAnalyticsEnabled get() = settings.getBooleanOrNull(AnalyticsKey) ?: false

    actual fun enableAnalytics() {
        settings.putBoolean(AnalyticsKey,true)
    }

    private fun getDirType() :BaseDirectory.ActiveBaseDirType{
        return if(settings.getStringOrNull(DirKey) == null) {
            // Default Dir
            BaseDirectory.ActiveBaseDirType.JavaFileBaseDir
        }else {
            // User Updated Dir
            BaseDirectory.ActiveBaseDirType.SafBaseDir
        }
    }

    actual fun setDownloadDirectory(newBasePath:File) = settings.putString(
        DirKey,
        newBasePath.documentFile!!.getFullPath()
    )

    fun setDownloadDirectory(treeUri:Uri) {
        try {
            fileManager.apply {
                registerBaseDir<SpotiFlyerBaseDir>(SpotiFlyerBaseDir(
                    { getDirType() },
                    getJavaFile = {
                        null
                    },
                    getSAFUri = {
                        treeUri
                    }
                ))
                fromUri(treeUri)?.let { createSnapshot(it,true) }
            }
        } catch (e:IllegalArgumentException) {
            methods.value.showPopUpMessage("This Directory is already set as Download Directory")
        }
        GlobalScope.launch {
            setDownloadDirectory(File(fileManager.fromUri(treeUri)))
            createDirectories()
        }
    }

    // Image Cache Path
    // We Will Handling Image relating operations using java.io.File (reason: Faster)
    actual val imageCachePath: String get() = methods.value.platformActions.imageCacheDir.absolutePath + "/"

    actual fun imageCacheDir(): File =  File(fileManager.fromPath(imageCachePath))

    // fun call in order to always access Updated Value
    actual fun defaultDir(): File = File(fileManager.newBaseDirectoryFile<SpotiFlyerBaseDir>())

    actual fun isPresent(file: File): Boolean = file.documentFile?.let { fileManager.exists(it) } ?: false

    actual fun createDirectory(dirPath: File , subDirectory:String?) {
        if(dirPath.documentFile != null) {
            if (subDirectory != null) {
                fileManager.createDir(dirPath.documentFile!!,subDirectory)
            }else {
                fileManager.create(dirPath.documentFile!!)
            }
        }
    }

    @Suppress("unused")
    actual suspend fun clearCache(): Unit = withContext(dispatcherIO) {
        try {
            java.io.File(imageCachePath).deleteRecursively()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    actual suspend fun saveFileWithMetadata(
        mp3ByteArray: ByteArray,
        trackDetails: TrackDetails,
        postProcess: (track: TrackDetails) -> Unit,
    ): Unit  = withContext(dispatcherIO) {
        val mediaFile = java.io.File(imageCachePath+"Tracks/"+ removeIllegalChars(trackDetails.title) + ".mp3")
        try {
            /*Make intermediate Dirs if they don't exist yet*/
            if(!mediaFile.exists()) {
                mediaFile.parentFile?.mkdirs()
            }
            // Write Bytes to Media File
            mediaFile.writeBytes(mp3ByteArray)

            // Add Metadata to Media File
            Mp3File(mediaFile)
                .removeAllTags()
                .setId3v1Tags(trackDetails)
                .setId3v2TagsAndSaveFile(trackDetails,mediaFile.absolutePath)

            // Copy File to Desired Location
            val documentFile = when(getDirType()){
                BaseDirectory.ActiveBaseDirType.SafBaseDir -> {
                    fileManager.fromUri(Uri.parse(trackDetails.outputFilePath))
                }
                BaseDirectory.ActiveBaseDirType.JavaFileBaseDir -> {
                    fileManager.fromPath(trackDetails.outputFilePath)
                }
            }.also {
                // Create Desired File if it doesn't exists yet
                fileManager.create(it!!)
            }

            try {
                fileManager.copyFileContents(
                    fileManager.fromRawFile(mediaFile),
                    documentFile!!
                )
                mediaFile.deleteOnExit()
            }catch (e:Exception) {
                e.printStackTrace()
            }

            documentFile?.let {
                addToLibrary(File(it),trackDetails)
            }
        }catch (e:Exception){
            e.printStackTrace()
            if(mediaFile.exists()) mediaFile.delete()
            logger.e { "${mediaFile.absolutePath} could not be created" }
        }
    }

    actual fun addToLibrary(file: File,track: TrackDetails) {
        try {
            when (getDirType()) {
                BaseDirectory.ActiveBaseDirType.SafBaseDir -> {
                    val values = ContentValues(4).apply {
                        put(MediaStore.Audio.Media.TITLE, track.title)
                        put(MediaStore.Audio.Media.DISPLAY_NAME, track.title)
                        put(MediaStore.Audio.Media.DATE_ADDED,
                            (System.currentTimeMillis() / 1000).toInt())
                        put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                    }
                    context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        values)
                }
                BaseDirectory.ActiveBaseDirType.JavaFileBaseDir -> {
                    file.documentFile?.getFullPath()?.let {
                        methods.value.platformActions.addToLibrary(it)
                    }
                }
            }
        } catch (e:Exception) { e.printStackTrace() }
    }

    actual suspend fun loadImage(url: String): Picture = withContext(dispatcherIO){
        val cachePath = imageCachePath + getNameURL(url)
        Picture(image = (loadCachedImage(cachePath) ?: freshImage(url))?.asImageBitmap())
    }

    private fun loadCachedImage(cachePath: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(cachePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            null
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    actual suspend fun cacheImage(image: Any, path: String):Unit = withContext(dispatcherIO) {
        try {
            java.io.File(path).parentFile?.mkdirs()
            FileOutputStream(path).use { out ->
                (image as? Bitmap)?.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun freshImage(url: String): Bitmap? = withContext(dispatcherIO) {
        try {
            val source = URL(url)
            val connection: HttpURLConnection = source.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.connect()

            val input: InputStream = connection.inputStream
            val result: Bitmap? = BitmapFactory.decodeStream(input)

            if (result != null) {
                GlobalScope.launch(Dispatchers.IO) {
                    cacheImage(result, imageCachePath + getNameURL(url))
                }
                result
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual val db: Database? = spotiFlyerDatabase.instance

    actual fun finalOutputPath(
        itemName: String,
        type: String,
        subFolder: String,
        extension: String,
    ):String = finalOutputFile(
            itemName,
            type,
            subFolder,
            extension,
        ).documentFile?.getFullPath() ?: throw(Exception("no path for $itemName"))

    actual fun finalOutputFile(
        itemName: String,
        type: String,
        subFolder: String,
        extension: String,
    ):File {
        // Create Intermediate Directories
        val file = fileManager.create(
            defaultDir().documentFile!!, //Base Dir
            DirectorySegment(removeIllegalChars(type)),
            DirectorySegment(removeIllegalChars(subFolder)),
            FileSegment(removeIllegalChars(itemName) + extension)
        )
        return File(file).also {
            if(fileManager.getLength(it.documentFile!!) == 0L) fileManager.delete(it.documentFile!!)
        }

    //?.clone(FileSegment(removeIllegalChars(itemName) + extension)))
    }
}
