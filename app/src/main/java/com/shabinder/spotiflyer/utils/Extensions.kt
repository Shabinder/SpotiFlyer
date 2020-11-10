/*
 * Copyright (C)  2020  Shabinder Singh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer.utils

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import com.shabinder.spotiflyer.utils.Provider.mainActivity

fun View.openPlatformOnClick(packageName:String, websiteAddress:String){
    val manager: PackageManager = mainActivity.packageManager
    try {
        val i = manager.getLaunchIntentForPackage(packageName)
            ?: throw PackageManager.NameNotFoundException()
        i.addCategory(Intent.CATEGORY_LAUNCHER)
        this.setOnClickListener { mainActivity.startActivity(i) }
    } catch (e: PackageManager.NameNotFoundException) {
        val uri: Uri =
            Uri.parse(websiteAddress)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        this.setOnClickListener { mainActivity.startActivity(intent) }
    }
}
fun View.openPlatformOnClick(websiteAddress:String){
    val uri: Uri =
        Uri.parse(websiteAddress)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    this.setOnClickListener { mainActivity.startActivity(intent) }
}