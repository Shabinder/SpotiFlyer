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

import android.annotation.SuppressLint
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import android.widget.Toast
import com.shabinder.spotiflyer.SharedViewModel
import com.shabinder.spotiflyer.models.DownloadObject
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.networking.YoutubeMusicApi
import com.shabinder.spotiflyer.networking.makeJsonBody
import com.shabinder.spotiflyer.utils.*
import com.shabinder.spotiflyer.utils.Provider.defaultDir
import com.shabinder.spotiflyer.utils.Provider.mainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

object DownloadHelper {

    var statusBar:TextView? = null
    var youtubeMusicApi: YoutubeMusicApi? = null
    var sharedViewModel: SharedViewModel? = null

    private var total = 0
    private var processed = 0
    var notFound = 0

    /**
     * Function To Download All Tracks Available in a List
     **/
    suspend fun downloadAllTracks(
        type:String,
        subFolder: String?,
        trackList: List<TrackDetails>) {
        resetStatusBar()// For New Download Request's Status
        val downloadList = ArrayList<DownloadObject>()
        withContext(Dispatchers.Main){
            total += trackList.size // Adding New Download List Count to StatusBar
            trackList.forEachIndexed { index, it ->
                if(!isOnline()){
                    showNoConnectionAlert()
                    return@withContext
                }
                if(it.downloaded == DownloadStatus.Downloaded){//Download Already Present!!
                    processed++
                    if(index == (trackList.size-1)){//LastElement
                        Handler().postDelayed({
                            //Delay is Added ,if a request is in processing it may finish
                            Log.i("Spotify Helper","Download Request Sent")
                            sharedViewModel?.uiScope?.launch (Dispatchers.Main){
                                showMessage("Download Started, Now You can leave the App!")
                            }
                            startService(mainActivity,downloadList)
                        },3000)
                    }
                }else{
                    val searchQuery = "${it.title} - ${it.artists.joinToString(",")}"
                    val jsonBody = makeJsonBody(searchQuery.trim()).toJsonString()
                    youtubeMusicApi?.getYoutubeMusicResponse(jsonBody)?.enqueue(
                        object : Callback<String>{
                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                sharedViewModel?.uiScope?.launch {
                                    val videoId = sortByBestMatch(
                                        getYTTracks(response.body().toString()),
                                        trackName = it.title,
                                        trackArtists = it.artists,
                                        trackDurationSec = it.durationSec
                                    ).keys.firstOrNull()
                                    Log.i("Spotify Helper Video ID",videoId ?: "Not Found")

                                    if(videoId.isNullOrBlank()) {notFound++ ; updateStatusBar()}
                                    else {//Found Youtube Video ID
                                        val outputFile: String =
                                            Environment.getExternalStorageDirectory().toString() + File.separator +
                                                    defaultDir +
                                                    removeIllegalChars(type) + File.separator +
                                                    (if (subFolder == null) { "" }
                                                    else { removeIllegalChars(subFolder) + File.separator }
                                                            + removeIllegalChars(it.title) + ".m4a")

                                        val downloadObject = DownloadObject(
                                            trackDetails = it,
                                            ytVideoId = videoId,
                                            outputFile = outputFile
                                        )
                                        processed++
                                        sharedViewModel?.uiScope?.launch(Dispatchers.Main) {
                                            updateStatusBar()
                                        }
                                        downloadList.add(downloadObject)
                                        if(index == (trackList.size-1)){//LastElement
                                            Handler().postDelayed({
                                                //Delay is Added ,if a request is in processing it may finish
                                                Log.i("Spotify Helper","Download Request Sent")
                                                sharedViewModel?.uiScope?.launch (Dispatchers.Main){
                                                    Toast.makeText(mainActivity,"Download Started, Now You can leave the App!", Toast.LENGTH_SHORT).show()
                                                }
                                                startService(mainActivity,downloadList)
                                            },5000)
                                        }
                                    }
                                }
                            }
                            override fun onFailure(call: Call<String>, t: Throwable) {
                                if(t.message.toString().contains("Failed to connect")) showMessage("Failed, Check Your Internet Connection!")
                                Log.i("YT API Req. Fail",t.message.toString())
                            }
                        }
                    )
                }
                updateStatusBar()
            }
            animateStatusBar()
        }
    }

    private fun resetStatusBar() {
        total = 0
        processed = 0
        notFound = 0
        updateStatusBar()
    }

    private fun animateStatusBar() {
        val anim: Animation = AlphaAnimation(0.3f, 0.9f)
        anim.duration = 1500 //You can manage the blinking time with this parameter
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        statusBar?.animation = anim
    }

    @SuppressLint("SetTextI18n")
    fun updateStatusBar() {
        statusBar!!.visibility = View.VISIBLE
        statusBar?.text = "Total: $total  ${getEmojiByUnicode(0x2705)}: $processed   ${getEmojiByUnicode(0x274C)}: $notFound"
    }
}