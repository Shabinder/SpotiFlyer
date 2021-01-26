package com.shabinder.common

expect open class PlatformDir() {
    fun isPresent(path:String):Boolean
    fun fileSeparator(): String
    fun defaultDir(): String
    fun imageDir(): String
}

fun PlatformDir.finalOutputDir(itemName:String ,type:String, subFolder:String,defaultDir:String,extension:String = ".mp3" ): String =
    defaultDir + removeIllegalChars(type) + this.fileSeparator() +
            if(subFolder.isEmpty())"" else { removeIllegalChars(subFolder) + this.fileSeparator()} +
            removeIllegalChars(itemName) + extension
