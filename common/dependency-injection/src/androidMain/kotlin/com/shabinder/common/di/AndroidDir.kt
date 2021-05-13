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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import androidx.documentfile.provider.DocumentFile
import co.touchlab.kermit.Kermit
import com.github.k1rakishou.fsaf.FileManager
import com.github.k1rakishou.fsaf.file.AbstractFile
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
import java.io.FileInputStream
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
    private val spotiFlyerDatabase: SpotiFlyerDatabase,
): KoinComponent {

    private val context: Context = get()
    val fileManager = FileManager(context)

    init {
        fileManager.registerBaseDir<SpotiFlyerBaseDir>(SpotiFlyerBaseDir({ getDirType() },
            getJavaFile = {
                java.io.File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString()
                            + "/SpotiFlyer/"
                )
            },
            getSAFUri = { null }
        ))
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
        newBasePath.documentFile?.getFullPath()!!
    )

    fun setDownloadDirectory(treeUri:Uri) {
        fileManager.registerBaseDir<SpotiFlyerBaseDir>(SpotiFlyerBaseDir(
            { getDirType() },
            getJavaFile = {
                null
            },
            getSAFUri = {
                treeUri
            }
        ))
    }

    @Suppress("DEPRECATION")// By Default Save Files to /Music/SpotiFlyer/
    private val defaultBaseDir = SpotiFlyerBaseDir({ getDirType() },
        getJavaFile = {java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString() + "/SpotiFlyer/")},
        getSAFUri = { null }
    )

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

        /*try {
            val yourAppDir =  File(dirPath)

            if (!yourAppDir.exists() && !yourAppDir.isDirectory) { // create empty directory
                if (yourAppDir.mkdirs()) { logger.i { "$dirPath created" } } else {
                    logger.e { "Unable to create Dir: $dirPath!" }
                }
            } else {
                logger.i { "$dirPath already exists" }
            }
        } catch (e: SecurityException) {
            //TRY USING SAF
            Log.d("Directory","USING SAF to create $dirPath")
            val file = DocumentFile.fromTreeUri(context, Uri.parse(defaultDir()))
            DocumentFile.fromFile()
        }*/
    }

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
        val songFile = java.io.File(imageCachePath+"Tracks/"+ removeIllegalChars(trackDetails.title) + ".mp3")
        try {

            /*Make intermediate Dirs if they don't exist yet*/
            if(!songFile.exists()) {
                songFile.parentFile?.mkdirs()
            }

            if(mp3ByteArray.isNotEmpty()) songFile.writeBytes(mp3ByteArray)

            Mp3File(songFile)
                .removeAllTags()
                .setId3v1Tags(trackDetails)
                .setId3v2TagsAndSaveFile(trackDetails,songFile.absolutePath)

            // Copy File to Desired Location
            val documentFile = when(getDirType()){
                BaseDirectory.ActiveBaseDirType.SafBaseDir -> {
                    fileManager.fromUri(Uri.parse(trackDetails.outputFilePath))
                }
                BaseDirectory.ActiveBaseDirType.JavaFileBaseDir -> {
                    fileManager.fromPath(trackDetails.outputFilePath)
                }
            }.also { fileManager.create(it!!) }

            try {
                fileManager.copyFileContents(
                    fileManager.fromRawFile(songFile),
                    documentFile!!
                )
                songFile.deleteOnExit()
                /*val inStream = FileInputStream(songFile)

                val buffer = ByteArray(1024)
                var readLen: Int
                while (inStream.read(buffer).also { readLen = it } != -1) {
                    outStream?.write(buffer, 0, readLen)
                }
                inStream.close()
                // write the output file (You have now copied the file)
                outStream?.flush()
                outStream?.close()*/

            }catch (e:Exception) {
                e.printStackTrace()
            }

            documentFile?.let {
                addToLibrary(File(it))
            }

            /*when (trackDetails.outputFilePath.substringAfterLast('.')) {
                ".mp3" -> {
                    Mp3File(songFile)
                        .removeAllTags()
                        .setId3v1Tags(trackDetails)
                        .setId3v2TagsAndSaveFile(trackDetails,songFile.absolutePath)

                    // Copy File to DocumentUri
                    val documentFile = DocumentFile.fromSingleUri(context,Uri.parse(trackDetails.outputFilePath))
                    try {
                        val outStream = context.contentResolver.openOutputStream(documentFile?.uri!!)
                        val inStream = FileInputStream(songFile)

                        val buffer = ByteArray(1024)
                        var readLen: Int
                        while (inStream.read(buffer).also { readLen = it } != -1) {
                            outStream?.write(buffer, 0, readLen)
                        }
                        inStream.close()
                        // write the output file (You have now copied the file)
                        outStream?.flush()
                        outStream?.close()

                    }catch (e:Exception) {
                        e.printStackTrace()
                    }

                    documentFile?.let {
                        addToLibrary(File(it))
                    }
                }
                ".m4a" -> {
                    *//*FFmpeg.executeAsync(
                        "-i ${m4aFile.absolutePath} -y -b:a 160k -acodec libmp3lame -vn ${m4aFile.absolutePath.substringBeforeLast('.') + ".mp3"}"
                    ){ _, returnCode ->
                        when (returnCode) {
                            Config.RETURN_CODE_SUCCESS  -> {
                                //FFMPEG task Completed
                                logger.d{ "Async command execution completed successfully." }
                                scope.launch {
                                    Mp3File(File(m4aFile.absolutePath.substringBeforeLast('.') + ".mp3"))
                                        .removeAllTags()
                                        .setId3v1Tags(trackDetails)
                                        .setId3v2TagsAndSaveFile(trackDetails)
                                    addToLibrary(m4aFile.absolutePath.substringBeforeLast('.') + ".mp3")
                                }
                            }
                            Config.RETURN_CODE_CANCEL -> {
                                logger.d{"Async command execution cancelled by user."}
                            }
                            else -> {
                                logger.d { "Async command execution failed with rc=$returnCode" }
                            }
                        }
                    }*//*
                }
                else -> {
                    // TODO
                }
            }*/
        }catch (e:Exception){
            withContext(Dispatchers.Main){
                //Toast.makeText(appContext,"Could Not Create File:\n${songFile.absolutePath}",Toast.LENGTH_SHORT).show()
            }
            if(songFile.exists()) songFile.delete()
            logger.e { "${songFile.absolutePath} could not be created" }
        }
    }

    actual fun addToLibrary(file: File) {
//        methods.value.platformActions.addToLibrary(path)
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
        val file = fileManager.create(
            defaultDir().documentFile!!, //Base Dir
            DirectorySegment(removeIllegalChars(type)),
            DirectorySegment(removeIllegalChars(subFolder)),

        )
        return File(file?.clone(FileSegment(removeIllegalChars(itemName) + extension)))/*.also {
            if(fileManager.getLength(it.documentFile!!) == 0L){
                fileManager.delete(it.documentFile!!)
            }
        }*/
        /*GlobalScope.launch {
            // Create Intermediate Directories
            var file = defaultDir().documentFile
            file = file.findFile(removeIllegalChars(type))
                ?: file.createDirectory(removeIllegalChars(type))
                    ?: throw Exception("Couldn't Find/Create $type Directory")

            if (subFolder.isNotEmpty()) file.findFile(removeIllegalChars(subFolder))
                ?: file.createDirectory(removeIllegalChars(subFolder))
                   ?: throw Exception("Couldn't Find/Create $subFolder Directory")

        }
        val sep = "%2F"
        val finalUri = defaultDir().documentFile.uri.toString() + sep +
                removeIllegalChars(type) + sep +
                removeIllegalChars(subFolder) + sep +
                removeIllegalChars(itemName) + extension
        return File(
            DocumentFile.fromSingleUri(context,Uri.parse(finalUri))!!
        ).also {
            Log.d("Final Output File",it.documentFile.uri.toString())
        }*/


        /*file = file?.findFile(removeIllegalChars(type))
                    ?: file?.createDirectory(removeIllegalChars(type))
                            ?: throw Exception("Couldn't Find/Create $type Directory")

        if (subFolder.isNotEmpty()) file = file.findFile(removeIllegalChars(subFolder))
            ?: file.createDirectory(removeIllegalChars(subFolder))
                    ?: throw Exception("Couldn't Find/Create $subFolder Directory")

        // TODO check Mime
        file = file.findFile(removeIllegalChars(itemName))
            ?: file.createFile("audio/mpeg",removeIllegalChars(itemName))
                    ?: throw Exception("Couldn't Find/Create ${removeIllegalChars(itemName) + extension} File")
        Log.d("Final Output File",file.uri.toString())

        return File(file).also {
            val size = it.documentFile.length()
            Log.d("File size", size.toString())
            if(size == 0L) it.documentFile.delete()
        }*/
    }
}
