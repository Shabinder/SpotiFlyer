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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.downloadHelper.SpotifyDownloadHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException

fun finalOutputDir(itemName:String,type:String, subFolder:String?=null): String{
    return Environment.getExternalStorageDirectory().toString() + File.separator +
            SpotifyDownloadHelper.defaultDir + SpotifyDownloadHelper.removeIllegalChars(type) + File.separator +
            (if(subFolder == null){""}else{ SpotifyDownloadHelper.removeIllegalChars(subFolder) + File.separator}
                    + SpotifyDownloadHelper.removeIllegalChars(itemName) +".mp3")
}

fun rotateAnim(view: View){
    val rotate = RotateAnimation(
        0F, 360F,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
    )
    rotate.duration = 1000
    rotate.repeatCount = Animation.INFINITE
    rotate.repeatMode = Animation.INFINITE
    rotate.interpolator = LinearInterpolator()
    view.animation = rotate
}


@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        Glide
            .with(imgView.context)
            .asFile()
            .load(imgUri)
            .placeholder(R.drawable.ic_song_placeholder)
            .error(R.drawable.ic_musicplaceholder)
            .listener(object:RequestListener<File>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<File>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.i("Glide","LoadFailed")
                    return false
                }

                override fun onResourceReady(
                    resource: File?,
                    model: Any?,
                    target: Target<File>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    CoroutineScope(Dispatchers.Main).launch {
                            try {
                                val file = File(
                                    Environment.getExternalStorageDirectory(),
                                    SpotifyDownloadHelper.defaultDir+".Images/" + imgUrl.substringAfterLast('/',imgUrl) + ".jpeg"
                                ) // the File to save , append increasing numeric counter to prevent files from getting overwritten.
                                val options = BitmapFactory.Options()
                                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                                val bitmap = BitmapFactory.decodeStream(FileInputStream(resource), null, options)
                                resource?.copyTo(file)
                                withContext(Dispatchers.Main){
                                    imgView.setImageBitmap(bitmap)
//                                    Log.i("Glide","imageSaved")
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                    }
                     return false
                }
            }).submit()
        }
    }

/**
 *Extension Function For Copying Files!
 **/
fun File.copyTo(file: File) {
    inputStream().use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}
fun createDirectory(dir:String){
    val yourAppDir = File(Environment.getExternalStorageDirectory(),
         dir)

    if(!yourAppDir.exists() && !yourAppDir.isDirectory)
    { // create empty directory
        if (yourAppDir.mkdirs())
        {Log.i("CreateDir","App dir created")}
        else
        {Log.w("CreateDir","Unable to create app dir!")}
    }
    else
    {Log.i("CreateDir","App dir already exists")}
}
