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
import java.io.File

/**
 * mainActivity Instance to use whereEver Needed , as Its God Activity.
 * (i.e, almost Active Throughout App's Lifecycle )
*/
val mainActivity
    get() = MainActivity.getInstance()

fun loadAllImages(context: Context? = mainActivity, images:List<String>? = null,source: Source) {
    val serviceIntent = Intent(context, ForegroundService::class.java)
    images?.let {  serviceIntent.putStringArrayListExtra("imagesList",(it + source.name) as ArrayList<String>) }
    context?.let { ContextCompat.startForegroundService(it, serviceIntent) }
}

fun downloadTracks(
    trackList: ArrayList<TrackDetails>,
    context: Context? = mainActivity
) {
    if(!trackList.isNullOrEmpty()){
        val serviceIntent = Intent(context, ForegroundService::class.java)
        serviceIntent.putParcelableArrayListExtra("object",trackList)
        context?.let { ContextCompat.startForegroundService(it, serviceIntent) }
    }
}

fun queryActiveTracks(context:Context? = mainActivity) {
    val serviceIntent = Intent(context, ForegroundService::class.java).apply {
        action = "query"
    }
    context?.let { ContextCompat.startForegroundService(it, serviceIntent) }
}

fun finalOutputDir(itemName:String ,type:String, subFolder:String,extension:String = ".mp3"): String{
    return Provider.defaultDir + removeIllegalChars(type) + File.separator +
            if(subFolder.isEmpty())"" else { removeIllegalChars(subFolder) + File.separator} +
            removeIllegalChars(itemName) + extension
}


/**
 * Util. Function To Check Connection Status
 * */
@Suppress("DEPRECATION")
fun isOnline(): Boolean {
    var result = false
    val connectivityManager =
        mainActivity.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
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
                (mainActivity.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
            result = netInfo != null && netInfo.isConnected
        }
    }
    return result
}


fun showDialog(title:String? = null, message: String? = null,response: String = "Ok"){
    //TODO
    Toast.makeText(mainActivity,title ?: "No Internet",Toast.LENGTH_SHORT).show()
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

fun createDirectories() {
    createDirectory(Provider.defaultDir)
    createDirectory(Provider.imageDir())
    createDirectory(Provider.defaultDir + "Tracks/")
    createDirectory(Provider.defaultDir + "Albums/")
    createDirectory(Provider.defaultDir + "Playlists/")
    createDirectory(Provider.defaultDir + "YT_Downloads/")
}
