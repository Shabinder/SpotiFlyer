package com.shabinder.common.di

import co.touchlab.kermit.Kermit
import com.shabinder.common.database.SpotiFlyerDatabase
import com.shabinder.common.di.preference.PreferenceManager
import com.shabinder.common.models.TrackDetails
import com.shabinder.database.Database
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDirectoryEnumerationSkipsHiddenFiles
import platform.Foundation.NSFileManager
import platform.Foundation.NSMusicDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSURLConnection
import platform.Foundation.NSURLRequest
import platform.Foundation.NSUserDomainMask
import platform.Foundation.URLByAppendingPathComponent
import platform.Foundation.sendSynchronousRequest
import platform.Foundation.writeToFile
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

actual class Dir actual constructor(
    val logger: Kermit,
    private val preferenceManager: PreferenceManager,
    spotiFlyerDatabase: SpotiFlyerDatabase,
) {

    actual fun isPresent(path: String): Boolean = NSFileManager.defaultManager.fileExistsAtPath(path)

    actual fun fileSeparator(): String = "/"

    private val defaultBaseDir = NSFileManager.defaultManager.URLForDirectory(NSMusicDirectory, NSUserDomainMask, null, true, null)!!.path!!

    // TODO Error Handling
    actual fun defaultDir(): String = (preferenceManager.downloadDir ?: defaultBaseDir) +
        fileSeparator() + "SpotiFlyer" + fileSeparator()

    private val defaultDirURL: NSURL by lazy {
        val musicDir = NSFileManager.defaultManager.URLForDirectory(NSMusicDirectory, NSUserDomainMask, null, true, null)!!
        musicDir.URLByAppendingPathComponent("SpotiFlyer", true)!!
    }

    actual fun imageCacheDir(): String = imageCacheURL.path!! + fileSeparator()

    private val imageCacheURL: NSURL by lazy {
        val cacheDir = NSFileManager.defaultManager.URLForDirectory(NSCachesDirectory, NSUserDomainMask, null, true, null)
        cacheDir?.URLByAppendingPathComponent("SpotiFlyer", true)!!
    }

    actual fun createDirectory(dirPath: String) {
        try {
            NSFileManager.defaultManager.createDirectoryAtPath(dirPath, true, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createDirectory(dirURL: NSURL) {
        try {
            NSFileManager.defaultManager.createDirectoryAtURL(dirURL, true, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual suspend fun cacheImage(image: Any, path: String): Unit = withContext(dispatcherIO) {
        try {
            (image as? UIImage)?.let {
                // We Will Be Using JPEG as default format everywhere
                UIImageJPEGRepresentation(it, 1.0)
                    ?.writeToFile(path, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual suspend fun loadImage(url: String, reqWidth: Int, reqHeight: Int): Picture = withContext(dispatcherIO) {
        try {
            val cachePath = imageCacheURL.URLByAppendingPathComponent(getNameURL(url))
            Picture(image = cachePath?.path?.let { loadCachedImage(it) } ?: loadFreshImage(url))
        } catch (e: Exception) {
            e.printStackTrace()
            Picture(null)
        }
    }

    private fun loadCachedImage(filePath: String, reqWidth: Int = 150, reqHeight: Int = 150): UIImage? {
        return try {
            UIImage.imageWithContentsOfFile(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun loadFreshImage(url: String, reqWidth: Int = 150, reqHeight: Int = 150): UIImage? = withContext(dispatcherIO) {
        try {
            val nsURL = NSURL(string = url)
            val data = NSURLConnection.sendSynchronousRequest(NSURLRequest.requestWithURL(nsURL), null, null)
            if (data != null) {
                UIImage.imageWithData(data)?.also {
                    GlobalScope.launch {
                        cacheImage(it, imageCacheDir() + getNameURL(url))
                    }
                }
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun clearCache(): Unit = withContext(dispatcherIO) {
        try {
            val fileManager = NSFileManager.defaultManager
            val paths = fileManager.contentsOfDirectoryAtURL(
                imageCacheURL,
                null,
                NSDirectoryEnumerationSkipsHiddenFiles,
                null
            )
            paths?.forEach {
                (it as? NSURL)?.let { nsURL ->
                    // Lets Remove Cached File
                    fileManager.removeItemAtURL(nsURL, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual suspend fun saveFileWithMetadata(
        mp3ByteArray: ByteArray,
        trackDetails: TrackDetails,
        postProcess: (track: TrackDetails) -> Unit
    ): Unit = withContext(dispatcherIO) {
        try {
            if (mp3ByteArray.isNotEmpty()) {
                mp3ByteArray.toNSData().writeToFile(
                    trackDetails.outputFilePath,
                    true
                )
            }
            when (trackDetails.outputFilePath.substringAfterLast('.')) {
                ".mp3" -> {
                    if (!isPresent(trackDetails.albumArtPath)) {
                        val imageData = downloadByteArray(
                            trackDetails.albumArtURL
                        )?.toNSData()
                        if (imageData != null) {
                            UIImage.imageWithData(imageData)?.also {
                                cacheImage(it, trackDetails.albumArtPath)
                            }
                        }
                    }
                    postProcess(trackDetails)
                    /*val file = TLAudio(trackDetails.outputFilePath)
                    file.addTagsAndSave(
                    trackDetails,
                    this::loadCachedImage,
                    this::addToLibrary
                )*/
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun addToLibrary(path: String) {
        // TODO
    }

    actual val db: Database? = spotiFlyerDatabase.instance
}
