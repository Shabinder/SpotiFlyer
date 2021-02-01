package com.shabinder.common

import co.touchlab.kermit.Kermit
import com.shabinder.common.utils.removeIllegalChars

expect fun openPlatform(platformID:String ,platformLink:String)

expect fun shareApp()

expect fun giveDonation()


expect open class Dir(
    logger: Kermit
) {
    fun isPresent(path:String):Boolean
    fun fileSeparator(): String
    fun defaultDir(): String
    fun imageDir(): String
    fun createDirectory(dirPath:String)
}
fun Dir.createDirectories() {
    createDirectory(defaultDir())
    createDirectory(imageDir())
    createDirectory(defaultDir() + "Tracks/")
    createDirectory(defaultDir() + "Albums/")
    createDirectory(defaultDir() + "Playlists/")
    createDirectory(defaultDir() + "YT_Downloads/")
}
fun Dir.finalOutputDir(itemName:String ,type:String, subFolder:String,defaultDir:String,extension:String = ".mp3" ): String =
    defaultDir + removeIllegalChars(type) + this.fileSeparator() +
            if(subFolder.isEmpty())"" else { removeIllegalChars(subFolder) + this.fileSeparator()} +
            removeIllegalChars(itemName) + extension
