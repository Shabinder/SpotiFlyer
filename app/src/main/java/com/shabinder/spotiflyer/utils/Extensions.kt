package com.shabinder.spotiflyer.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import com.github.kiulian.downloader.model.YoutubeVideo
import com.github.kiulian.downloader.model.formats.Format
import com.github.kiulian.downloader.model.quality.AudioQuality
import com.shabinder.spotiflyer.BuildConfig
import com.shabinder.spotiflyer.MainActivity
import java.io.File


/*
* Only Log in Debug Mode
**/
fun log(tag:String? = "SpotiFlyer",message:String? = "null"){
    if (BuildConfig.DEBUG) {
        Log.d(tag ?: "spotiflyer", message ?: "null")
    }
}
fun MainActivity.requestStoragePermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.requestPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            786
        )
    }
}
fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG , quality: Int = 90) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}
fun YoutubeVideo.getData(): Format?{
    return try {
        findAudioWithQuality(AudioQuality.medium)?.get(0) as Format
    } catch (e: java.lang.IndexOutOfBoundsException) {
        try {
            findAudioWithQuality(AudioQuality.high)?.get(0) as Format
        } catch (e: java.lang.IndexOutOfBoundsException) {
            try {
                findAudioWithQuality(AudioQuality.low)?.get(0) as Format
            } catch (e: java.lang.IndexOutOfBoundsException) {
                log("YTDownloader", e.toString())
                null
            }
        }
    }
}
fun openPlatform(packageName:String, websiteAddress:String,context: Context){
    val manager: PackageManager = context.packageManager
    try {
        val intent = manager.getLaunchIntentForPackage(packageName)
            ?: throw PackageManager.NameNotFoundException()
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        context.startActivity(intent)
    } catch (e: PackageManager.NameNotFoundException) {
        val uri: Uri =
            Uri.parse(websiteAddress)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }
}

fun openPlatform(websiteAddress:String,context: Context){
    val uri = Uri.parse(websiteAddress)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}