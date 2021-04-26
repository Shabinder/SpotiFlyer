package com.shabinder.common.di

import co.touchlab.kermit.Kermit
import com.shabinder.common.models.TrackDetails
import com.shabinder.database.Database
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import platform.Foundation.*
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import cocoapods.TagLibIOS.TLAudio

actual class Dir actual constructor(
    private val logger: Kermit,
    private val database: Database?
) {

    init {
        createDirectories()
    }

    actual fun isPresent(path: String): Boolean = NSFileManager.defaultManager.fileExistsAtPath(path)

    actual fun fileSeparator(): String = "/"

    // TODO Error Handling
    actual fun defaultDir(): String = defaultDirURL.path!!

    val defaultDirURL: NSURL by lazy {
        val musicDir = NSFileManager.defaultManager.URLForDirectory(NSMusicDirectory, NSUserDomainMask,null,true,null)!!
        musicDir.URLByAppendingPathComponent("SpotiFlyer",true)!!
    }

    actual fun imageCacheDir(): String = imageCacheURL.path!!

    val imageCacheURL: NSURL by lazy {
        val cacheDir = NSFileManager.defaultManager.URLForDirectory(NSCachesDirectory, NSUserDomainMask,null,true,null)
        cacheDir?.URLByAppendingPathComponent("SpotiFlyer",true)!!
    }

    actual fun createDirectory(dirPath: String) {
        try {
            NSFileManager.defaultManager.createDirectoryAtPath(dirPath,true,null,null)
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    fun createDirectory(dirURL: NSURL) {
        try {
            NSFileManager.defaultManager.createDirectoryAtURL(dirURL,true,null,null)
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    actual suspend fun cacheImage(image: Any, path: String) {
        try {
            (image as? UIImage)?.let {
                // We Will Be Using JPEG as default format everywhere
                UIImageJPEGRepresentation(it,1.0)
                    ?.writeToFile(path,true)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    actual suspend fun loadImage(url: String): Picture {
        return try {
            val cachePath = imageCacheURL.URLByAppendingPathComponent(getNameURL(url))
            Picture(image = cachePath?.path?.let { loadCachedImage(it) } ?: loadFreshImage(url))
        } catch (e: Exception) {
            e.printStackTrace()
            Picture(null)
        }
    }

    private fun loadCachedImage(filePath: String): UIImage? {
        return try {
            UIImage.imageWithContentsOfFile(filePath)
        }catch (e:Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun loadFreshImage(url: String):UIImage? {
        return try {
            val nsURL = NSURL(url)
            val data = NSURLConnection.sendSynchronousRequest(NSURLRequest.requestWithURL(nsURL),null,null)
            if (data != null) {
                UIImage.imageWithData(data)?.also {
                    GlobalScope.launch {
                        cacheImage(it, imageCacheDir() + getNameURL(url))
                    }
                }
            }else null
        }catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun clearCache() {
        try {
            val fileManager = NSFileManager.defaultManager
            val paths = fileManager.contentsOfDirectoryAtURL(imageCacheURL,
                null,
                NSDirectoryEnumerationSkipsHiddenFiles,
                null
            )
            paths?.forEach {
                (it as? NSURL)?.let { nsURL ->
                    // Lets Remove Cached File
                    fileManager.removeItemAtURL(nsURL,null)
                }
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }

    }

    actual suspend fun saveFileWithMetadata(
        mp3ByteArray: ByteArray,
        trackDetails: TrackDetails
    ) {
        when (trackDetails.outputFilePath.substringAfterLast('.')) {
            ".mp3" -> {
                val file = TLAudio(trackDetails.outputFilePath)
                file.addTagsAndSave(
                    trackDetails,
                    this::loadCachedImage,
                    this::addToLibrary
                )
            }
        }
    }

    actual fun addToLibrary(path: String) {
        // TODO
    }

    actual val db: Database? = database
}