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

package com.shabinder.spotiflyer.downloadHelper

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.model.formats.Format
import com.github.kiulian.downloader.model.quality.AudioQuality
import com.shabinder.spotiflyer.models.DownloadObject
import com.shabinder.spotiflyer.models.Track
import com.shabinder.spotiflyer.ui.spotify.SpotifyViewModel
import com.shabinder.spotiflyer.utils.YoutubeMusicApi
import com.shabinder.spotiflyer.utils.getEmojiByUnicode
import com.shabinder.spotiflyer.utils.makeJsonBody
import com.shabinder.spotiflyer.worker.ForegroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

object SpotifyDownloadHelper {
    var context : Context? = null
    var statusBar:TextView? = null
    var youtubeMusicApi:YoutubeMusicApi? = null
    val defaultDir = Environment.DIRECTORY_MUSIC + File.separator + "SpotiFlyer" + File.separator
    var spotifyViewModel: SpotifyViewModel? = null
    var total = 0
    var Processed = 0
    var notFound = 0

    /**
     * Function To Download All Tracks Available in a List
     **/
    suspend fun downloadAllTracks(
        type:String,
        subFolder: String?,
        trackList: List<Track>, ytDownloader: YoutubeDownloader?) {
        withContext(Dispatchers.Main){
            total += trackList.size // Adding New Download List Count to StatusBar
            trackList.forEach {
                if(it.downloaded == "Downloaded"){//Download Already Present!!
                    Processed++
                }else{
                    val artistsList = mutableListOf<String>()
                    it.artists?.forEach { artist -> artistsList.add(artist!!.name!!) }
                    searchYTMusic(type,subFolder,ytDownloader,"${it.name} - ${artistsList.joinToString(",")}", it)
                }
                updateStatusBar()
            }
            animateStatusBar()
        }
    }


    suspend fun searchYTMusic(type:String,
                              subFolder:String?,
                              ytDownloader: YoutubeDownloader?,
                              searchQuery: String,
                              track: Track){
        val jsonBody = makeJsonBody(searchQuery.trim())
        youtubeMusicApi?.getYoutubeMusicResponse(jsonBody)?.enqueue(
            object : Callback<String>{
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    spotifyViewModel?.uiScope?.launch {
                        Log.i("YT API BODY",response.body().toString())
                        Log.i("YT Search Query",searchQuery)
                        getYTLink(type,subFolder,ytDownloader,response.body().toString(),track)
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.i("YT API Fail",t.message.toString())
                }
            }
        )

    }


    fun updateStatusBar() {
        statusBar!!.visibility = View.VISIBLE
        statusBar?.text = "Total: $total  ${getEmojiByUnicode(0x2705)}: $Processed   ${getEmojiByUnicode(0x274C)}: $notFound"
    }


    fun downloadFile(subFolder: String?, type: String, track:Track, ytDownloader: YoutubeDownloader?, id: String) {
        spotifyViewModel!!.uiScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val video = ytDownloader?.getVideo(id)
                    val format: Format? = try {
                        video?.findAudioWithQuality(AudioQuality.high)?.get(0) as Format
                    } catch (e: java.lang.IndexOutOfBoundsException) {
                        try {
                            video?.findAudioWithQuality(AudioQuality.medium)?.get(0) as Format
                        } catch (e: java.lang.IndexOutOfBoundsException) {
                            try {
                                video?.findAudioWithQuality(AudioQuality.low)?.get(0) as Format
                            } catch (e: java.lang.IndexOutOfBoundsException) {
                                Log.i("YTDownloader", e.toString())
                                null
                            }
                        }
                    }
                    format?.let {
                        val url: String = format.url()
                        Log.i("DHelper Link Found", url)
                        val outputFile: String =
                            Environment.getExternalStorageDirectory().toString() + File.separator +
                                    defaultDir + removeIllegalChars(type) + File.separator + (if (subFolder == null) {
                                ""
                            } else {
                                removeIllegalChars(subFolder) + File.separator
                            } + removeIllegalChars(track.name!!) + ".m4a")

                        val downloadObject = DownloadObject(
                            track = track,
                            url = url,
                            outputDir = outputFile
                        )
                        Log.i("DH", outputFile)
                        startService(context!!, downloadObject)
                        Processed++
                        spotifyViewModel?.uiScope?.launch(Dispatchers.Main) {
                            updateStatusBar()
                        }
                    }
                }catch (e: com.github.kiulian.downloader.YoutubeException){
                    Log.i("DH", e.message)
                }
            }
        }
    }

    fun startService(context:Context,obj:DownloadObject? = null ) {
        val serviceIntent = Intent(context, ForegroundService::class.java)
        obj?.let {  serviceIntent.putExtra("object",it) }
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    /**
     * Removing Illegal Chars from File Name
     * **/
    fun removeIllegalChars(fileName: String): String? {
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

    private fun animateStatusBar() {
        val anim: Animation = AlphaAnimation(0.0f, 0.9f)
        anim.duration = 650 //You can manage the blinking time with this parameter
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        statusBar?.animation = anim
    }
}