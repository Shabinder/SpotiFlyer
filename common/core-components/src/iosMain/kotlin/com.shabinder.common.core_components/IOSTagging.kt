package com.shabinder.common.di
/*

import cocoapods.TagLibIOS.TLAudio
import com.shabinder.common.models.TrackDetails
import platform.Foundation.NSNumber
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

suspend fun TLAudio.addTagsAndSave(
    trackDetails: TrackDetails,
    loadCachedImage:(path:String)->UIImage?,
    addToLibrary:(path:String)->Unit
) {
    title = trackDetails.title
    artist = trackDetails.artists.joinToString(", ")
    album = trackDetails.albumName
    comment = trackDetails.comment
    try { trackDetails.year?.substring(0, 4)?.toInt()?.let { year = NSNumber(it) } } catch (e: Exception) {}
    try {
        val image = loadCachedImage(trackDetails.albumArtPath)
        if (image != null) {
            setFrontCoverPicture(UIImageJPEGRepresentation(image,1.0))
            save()
            addToLibrary(trackDetails.outputFilePath)
        }
        throw Exception("Cached Image not Present,Trying to Download...")
    } catch (e: Exception){
        e.printStackTrace()
        try {
            downloadByteArray(trackDetails.albumArtURL)?.toNSData()?.also {
                setFrontCoverPicture(it)
                save()
                addToLibrary(trackDetails.outputFilePath)
            }
        } catch (e: Exception){ e.printStackTrace() }
    }
}*/
