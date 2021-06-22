package com.shabinder.spotiflyer.service

import android.util.Log
import java.io.File

/**
 * Cleaning All Residual Files except Mp3 Files
 **/
fun cleanFiles(dir: File) {
    try {
        Log.d("File Cleaning","Starting Cleaning in ${dir.path} ")
        val fList = dir.listFiles()
        fList?.let {
            for (file in fList) {
                if (file.isDirectory) {
                    cleanFiles(file)
                } else if (file.isFile) {
                    if (file.path.toString().substringAfterLast(".") != "mp3") {
                        Log.d("Files Cleaning","Cleaning ${file.path}")
                        file.delete()
                    }
                }
            }
        }
    } catch (e: Exception) { e.printStackTrace() }
}

