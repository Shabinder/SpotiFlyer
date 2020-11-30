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
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
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

fun View.rotate(){
    val rotate = RotateAnimation(
        0F, 360F,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
    )
    rotate.duration = 2000
    rotate.repeatCount = Animation.INFINITE
    rotate.repeatMode = Animation.INFINITE
    rotate.interpolator = LinearInterpolator()
    this.animation = rotate
}

fun View.visible(){
    this.visibility = View.VISIBLE
}
fun View.gone(){
    this.visibility = View.GONE
}
fun View.invisible(){
    this.visibility = View.INVISIBLE
}