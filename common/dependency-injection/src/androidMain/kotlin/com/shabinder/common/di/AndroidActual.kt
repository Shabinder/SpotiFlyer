package com.shabinder.common.di

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.shabinder.common.database.appContext
import com.shabinder.common.models.TrackDetails

actual fun openPlatform(packageID:String, platformLink:String){
    val manager: PackageManager = appContext.packageManager
    try {
        val intent = manager.getLaunchIntentForPackage(packageID)
            ?: throw PackageManager.NameNotFoundException()
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        appContext.startActivity(intent)
    } catch (e: PackageManager.NameNotFoundException) {
        val uri: Uri =
            Uri.parse(platformLink)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        appContext.startActivity(intent)
    }
}

actual fun shareApp(){
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Hey, checkout this excellent Music Downloader http://github.com/Shabinder/SpotiFlyer")
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    appContext.startActivity(shareIntent)
}

actual fun giveDonation(){
    //TODO
}

actual suspend fun downloadTracks(
    list: List<TrackDetails>,
    getYTIDBestMatch:suspend (String,TrackDetails)->String?,
    saveFileWithMetaData:suspend (mp3ByteArray:ByteArray, trackDetails: TrackDetails) -> Unit
){
    //TODO
}