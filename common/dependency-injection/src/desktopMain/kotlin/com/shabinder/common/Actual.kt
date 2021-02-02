package com.shabinder.common

import co.touchlab.kermit.Kermit
import java.io.File

actual fun openPlatform(platformID:String ,platformLink:String){
    //TODO
}

actual fun shareApp(){
    //TODO
}

actual fun giveDonation(){
    //TODO
}

actual fun downloadTracks(list: List<TrackDetails>){
    //TODO
}

actual open class Dir actual constructor(private val logger: Kermit) {

    actual fun fileSeparator(): String = File.separator

    actual fun imageDir(): String = System.getProperty("user.home") + ".images" + File.separator

    @Suppress("DEPRECATION")
    actual fun defaultDir(): String = System.getProperty("user.home") + fileSeparator() +
            "SpotiFlyer" + fileSeparator()

    actual fun isPresent(path: String): Boolean = File(path).exists()

    actual fun createDirectory(dirPath:String){
        val yourAppDir = File(dirPath)

        if(!yourAppDir.exists() && !yourAppDir.isDirectory)
        { // create empty directory
            if (yourAppDir.mkdirs())
            {logger.i{"$dirPath created"}}
            else
            {
                logger.e{"Unable to create Dir: $dirPath!"}
            }
        }
        else {
            logger.i { "$dirPath already exists" }
        }
    }
}