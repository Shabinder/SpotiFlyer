package com.shabinder.spotiflyer.service

import android.util.Log
import java.io.File

/**
 * Cleaning All Residual Files except Mp3 Files
 **/
fun cleanFiles(dir: File) {
    try {
        Log.d("File Cleaning", "Starting Cleaning in ${dir.path} ")
        val fList = dir.listFiles()
        fList?.let {
            for (file in fList) {
                if (file.isDirectory) {
                    cleanFiles(file)
                } else if (file.isFile) {
                    val filePath = file.path.toString()
                    if (filePath.substringAfterLast(".") != "mp3" || filePath.isTempFile()) {
                        Log.d("Files Cleaning", "Cleaning $filePath")
                        file.delete()
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun String.isTempFile(): Boolean {
    return substringBeforeLast(".").takeLast(5) == ".temp"
}
