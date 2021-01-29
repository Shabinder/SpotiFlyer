package com.shabinder.common

import java.io.File

actual open class Dir{

    actual fun fileSeparator(): String = File.separator

    actual fun imageDir(): String = System.getProperty("user.home") + ".images" + File.separator

    @Suppress("DEPRECATION")
    actual fun defaultDir(): String = System.getProperty("user.home") + fileSeparator() +
            "SpotiFlyer" + fileSeparator()

    actual fun isPresent(path: String): Boolean = File(path).exists()

}