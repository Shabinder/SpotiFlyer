package com.shabinder.common.di

import org.khronos.webgl.ArrayBuffer
import org.w3c.files.Blob

@JsModule("browser-id3-writer")
@JsNonModule
external class ID3Writer(a: ArrayBuffer) {
    fun setFrame(frameName:String,frameValue:Any):ID3Writer
    fun removeTag()
    fun addTag():ArrayBuffer
    fun getBlob():Blob
    fun getURL():String
    fun revokeURL()
}