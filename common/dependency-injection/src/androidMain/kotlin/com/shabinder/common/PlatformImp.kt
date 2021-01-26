package com.shabinder.common

import android.content.Context
import android.os.Environment
import java.io.File

actual open class PlatformDir {

    actual fun fileSeparator(): String = File.separator

//    actual fun imageDir(): String = context.cacheDir.absolutePath + File.separator
    actual fun imageDir(): String = defaultDir() + File.separator + ".images" + File.separator

    @Suppress("DEPRECATION")
    actual fun defaultDir(): String =
        Environment.getExternalStorageDirectory().toString() + File.separator +
                Environment.DIRECTORY_MUSIC + File.separator +
                "SpotiFlyer"+ File.separator


    actual fun isPresent(path: String): Boolean = File(path).exists()
}