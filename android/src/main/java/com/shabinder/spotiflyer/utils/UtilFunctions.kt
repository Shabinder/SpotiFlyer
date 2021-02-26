package com.shabinder.spotiflyer.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom

fun Activity.checkIfLatestVersion() {
    AppUpdater(this,0).run {
        setDisplay(Display.NOTIFICATION)
        showAppUpdated(false)//true:Show App is Updated Dialog
        setUpdateFrom(UpdateFrom.XML)
        setUpdateXML("https://raw.githubusercontent.com/Shabinder/SpotiFlyer/Compose/app/src/main/res/xml/app_update.xml")
        setCancelable(false)
        start()
    }
}

fun Activity.checkPermissions():Boolean{
    return (ContextCompat
        .checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            &&
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == PackageManager.PERMISSION_GRANTED)

}

@SuppressLint("BatteryLife", "ObsoleteSdkInt")
fun Activity.disableDozeMode(requestCode:Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val pm =
            this.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(packageName)
        if (!isIgnoringBatteryOptimizations) {
            val intent = Intent().apply{
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = Uri.parse("package:$packageName")
            }
            startActivityForResult(intent, requestCode)
        }
    }
}
@SuppressLint("ObsoleteSdkInt")
fun Activity.requestStoragePermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.requestPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            786
        )
    }
}