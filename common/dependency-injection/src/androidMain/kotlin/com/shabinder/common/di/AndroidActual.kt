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
    //TODO
}

actual fun giveDonation(){
    //TODO
}

actual fun downloadTracks(list: List<TrackDetails>){
    //TODO
}