package com.shabinder.common.di.worker

import co.touchlab.kermit.Kermit
import com.github.k1rakishou.fsaf.FileManager
import com.github.k1rakishou.fsaf.file.AbstractFile
import java.io.File

/**
 * Cleaning All Residual Files except Mp3 Files
 **/
fun cleanFiles(dir: File,logger: Kermit) {
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
    } catch (e:Exception) { e.printStackTrace() }
}
/**
 * Cleaning All Residual Files except Mp3 Files
 **/
fun cleanFiles(directory: AbstractFile,fm: FileManager,logger: Kermit) {
    try {
        logger.d("Files Cleaning") { "Starting Cleaning in ${directory.getFullPath()} " }
        val fList = fm.listFiles(directory)
        for (file in fList) {
            if (fm.isDirectory(file)) {
                cleanFiles(file, fm, logger)
            } else if (fm.isFile(file)) {
                if (file.getFullPath().substringAfterLast(".") != "mp3") {
                    logger.d("Files Cleaning") { "Cleaning ${file.getFullPath()}" }
                    fm.delete(file)
                }
            }
        }
    } catch (e:Exception) { e.printStackTrace() }
}