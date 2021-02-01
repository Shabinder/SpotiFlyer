package com.shabinder.common

import android.content.Context
import android.os.Environment
import co.touchlab.kermit.Kermit
import com.shabinder.common.database.appContext
import java.io.File

actual open class Dir actual constructor(logger: Kermit) {

    private val context:Context
        get() = appContext
    
    actual fun fileSeparator(): String = File.separator

    actual fun imageDir(): String = context.cacheDir.absolutePath + File.separator

    @Suppress("DEPRECATION")
    actual fun defaultDir(): String =
        Environment.getExternalStorageDirectory().toString() + File.separator +
                Environment.DIRECTORY_MUSIC + File.separator +
                "SpotiFlyer"+ File.separator

    actual fun isPresent(path: String): Boolean = File(path).exists()
    actual fun createDirectory(dirPath: String) {
    }
}