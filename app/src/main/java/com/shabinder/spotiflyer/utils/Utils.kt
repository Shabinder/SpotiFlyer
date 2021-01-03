/*
 * Copyright (c)  2021  Shabinder Singh
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
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.shabinder.spotiflyer.MainActivity
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.worker.ForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

val sharedViewModel
    get() = MainActivity.getSharedViewModel()

fun loadAllImages( images:List<String>? = null,source: Source, context: Context) {
    val serviceIntent = Intent(context, ForegroundService::class.java)
    images?.let {  serviceIntent.putStringArrayListExtra("imagesList",(it + source.name) as ArrayList<String>) }
    context.let { ContextCompat.startForegroundService(it, serviceIntent) }
}

fun downloadTracks(
    trackList: ArrayList<TrackDetails>,
    context: Context
) {
    if(!trackList.isNullOrEmpty()){
        loadAllImages(
            trackList.map { it.albumArtURL },
            trackList.first().source,
            context
        )
        val serviceIntent = Intent(context, ForegroundService::class.java)
        serviceIntent.putParcelableArrayListExtra("object",trackList)
        context.let { ContextCompat.startForegroundService(it, serviceIntent) }
    }
}

/**
 * Util. Function To Check Connection Status
 * */
@Suppress("DEPRECATION")
fun isOnline(ctx:Context): Boolean {
    var result = false
    val connectivityManager =
        ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    connectivityManager?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            it.getNetworkCapabilities(connectivityManager.activeNetwork)?.apply {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->  true
                    else -> false
                }
            }
        } else {
            val netInfo =
                (ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
            result = netInfo != null && netInfo.isConnected
        }
    }
    return result
}


fun showDialog(title:String? = null, message: String? = null,response: String = "Ok"){
    //TODO
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(MainActivity.getInstance(),title ?: "No Internet",Toast.LENGTH_SHORT).show()
    }
}

fun createDirectory(dir:String){
    val yourAppDir = File(dir)

    if(!yourAppDir.exists() && !yourAppDir.isDirectory)
    { // create empty directory
        if (yourAppDir.mkdirs())
        {log("CreateDir","$dir created")}
        else
        {
            Log.w("CreateDir","Unable to create Dir: $dir!")}
    }
    else
    {log("CreateDir","$dir already exists")}
}
/**
 * Removing Illegal Chars from File Name
 * **/
fun removeIllegalChars(fileName: String): String {
    val illegalCharArray = charArrayOf(
        '/',
        '\n',
        '\r',
        '\t',
        '\u0000',
        '\u000C',
        '`',
        '?',
        '*',
        '\\',
        '<',
        '>',
        '|',
        '\"',
        '.',
        '-',
        '\''
    )

    var name = fileName
    for (c in illegalCharArray) {
        name = fileName.replace(c, '_')
    }
    name = name.replace("\\s".toRegex(), "_")
    name = name.replace("\\)".toRegex(), "")
    name = name.replace("\\(".toRegex(), "")
    name = name.replace("\\[".toRegex(), "")
    name = name.replace("]".toRegex(), "")
    name = name.replace("\\.".toRegex(), "")
    name = name.replace("\"".toRegex(), "")
    name = name.replace("\'".toRegex(), "")
    name = name.replace(":".toRegex(), "")
    name = name.replace("\\|".toRegex(), "")
    return name
}

fun createDirectories(defaultDir:String,imageDir:String) {
    createDirectory(defaultDir)
    createDirectory(imageDir)
    createDirectory(defaultDir + "Tracks/")
    createDirectory(defaultDir + "Albums/")
    createDirectory(defaultDir + "Playlists/")
    createDirectory(defaultDir + "YT_Downloads/")
}
