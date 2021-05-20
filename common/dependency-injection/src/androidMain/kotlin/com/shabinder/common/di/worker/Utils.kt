package com.shabinder.common.di.worker

import co.touchlab.kermit.Kermit
import java.io.File

/**
 * Cleaning All Residual Files except Mp3 Files
 **/
fun cleanFiles(dir: File, logger: Kermit) {
    try {
        logger.d("File Cleaning") { "Starting Cleaning in ${dir.path} " }
        val fList = dir.listFiles()
        fList?.let {
            for (file in fList) {
                if (file.isDirectory) {
                    cleanFiles(file, logger)
                } else if (file.isFile) {
                    if (file.path.toString().substringAfterLast(".") != "mp3") {
                        logger.d("Files Cleaning") { "Cleaning ${file.path}" }
                        file.delete()
                    }
                }
            }
        }
    } catch (e: Exception) { e.printStackTrace() }
}
