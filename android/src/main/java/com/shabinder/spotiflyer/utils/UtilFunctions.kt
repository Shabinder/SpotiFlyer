/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
    AppUpdater(this, 0).run {
        setDisplay(Display.NOTIFICATION)
        showAppUpdated(false) // true:Show App is Updated Dialog
        setUpdateFrom(UpdateFrom.XML)
        setUpdateXML("https://raw.githubusercontent.com/Shabinder/SpotiFlyer/Compose/app/src/main/res/xml/app_update.xml")
        setCancelable(false)
        start()
    }
}

fun Activity.checkPermissions(): Boolean = ContextCompat
    .checkSelfPermission(
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED &&
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else true

@SuppressLint("BatteryLife", "ObsoleteSdkInt")
fun Activity.disableDozeMode(requestCode: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val pm =
            this.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(packageName)
        if (!isIgnoringBatteryOptimizations) {
            val intent = Intent().apply {
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
/*
fun Activity.requestBroaderStoragePermission() {
    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
    startActivity(intent)
}*/
