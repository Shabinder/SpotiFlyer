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
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.model.formats.Format
import com.github.kiulian.downloader.model.quality.AudioQuality
import com.shabinder.spotiflyer.models.DownloadObject
import com.shabinder.spotiflyer.models.Track
import com.shabinder.spotiflyer.ui.spotify.SpotifyFragment
import com.shabinder.spotiflyer.ui.spotify.SpotifyViewModel
import com.shabinder.spotiflyer.worker.ForegroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object SpotifyDownloadHelper {
    var webView:WebView? = null
    var context : Context? = null
    var statusBar:TextView? = null
    val defaultDir = Environment.DIRECTORY_MUSIC + File.separator + "SpotiFlyer" + File.separator
    var spotifyViewModel: SpotifyViewModel? = null
    private var isBrowserLoading = false
    private var total = 0
    private var Processed = 0
    private var listProcessed:Boolean = false
    var youtubeList = mutableListOf<YoutubeRequest>()

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
                    if(isBrowserLoading){//WebView Busy!!
                        if (listProcessed){//Previous List request progress check
                            getYTLink(null,type,subFolder,ytDownloader,"${it.name} ${it.artists?.get(0)?.name ?:""}", it)
                            listProcessed = false//Notifying A list Processing Started
                        }else{//Adding Requests to a Queue
                            youtubeList.add(YoutubeRequest(null,type,subFolder,ytDownloader,"${it.name} ${it.artists?.get(0)?.name ?:""}", it))
                        }
                    }else{
                        getYTLink(null,type,subFolder,ytDownloader,"${it.name} ${it.artists?.get(0)?.name ?:""}", it)
                    }
                }
                updateStatusBar()
            }
            animateStatusBar()
        }
    }



    //TODO CleanUp here and there!!
    @SuppressLint("SetJavaScriptEnabled")
    suspend fun getYTLink(spotifyFragment: SpotifyFragment? = null,
                          type:String,
                          subFolder:String?,
                          ytDownloader: YoutubeDownloader?,
                          searchQuery: String,
                          track: Track){
        val searchText = searchQuery.replace("\\s".toRegex(), "+")
        val url = "https://www.youtube.com/results?sp=EgIQAQ%253D%253D&q=$searchText"
        Log.i("DH YT LINK ",url)
        applyWebViewSettings(webView!!)
        withContext(Dispatchers.Main){
            isBrowserLoading = true
            webView!!.loadUrl(url)
            webView!!.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.evaluateJavascript(
                        "document.getElementsByClassName(\"yt-simple-endpoint style-scope ytd-video-renderer\")[0].href"
                        ,object :ValueCallback<String>{
                            override fun onReceiveValue(value: String?) {
                                Log.i("YT-id",value.toString().replace("\"",""))
                                val id = value!!.substringAfterLast("=", "error").replace("\"","")
                                Log.i("YT-id",id)
                                if(id !="error"){//Link extracting error
                                    spotifyFragment?.showToast("Starting Download")
                                    Processed++
                                    if(Processed == total)listProcessed = true //List Processesd
                                    updateStatusBar()
                                    downloadFile(subFolder, type, track,ytDownloader,id)
                                }
                                if(youtubeList.isNotEmpty()){
                                    val request = youtubeList[0]
                                    spotifyViewModel!!.uiScope.launch {
                                        getYTLink(request.spotifyFragment,request.type,request.subFolder,request.ytDownloader,request.searchQuery,request.track)
                                    }
                                    youtubeList.remove(request)
                                    if(youtubeList.size == 0){//list processing completed , webView is free again!
                                        isBrowserLoading = false
                                    }
                                }
                            }
                    }   )
                }
            }
        }

    }

    private fun updateStatusBar() {
        statusBar!!.visibility = View.VISIBLE
        statusBar?.text = "Total: $total  Processed: $Processed"
    }


    fun downloadFile(subFolder: String?, type: String, track:Track, ytDownloader: YoutubeDownloader?, id: String) {
        spotifyViewModel!!.uiScope.launch {
            withContext(Dispatchers.IO) {
                val video = ytDownloader?.getVideo(id)
                val detail = video?.details()
                val format:Format? =try {
                    video?.findAudioWithQuality(AudioQuality.high)?.get(0) as Format
                }catch (e:java.lang.IndexOutOfBoundsException){
                    try {
                        video?.findAudioWithQuality(AudioQuality.medium)?.get(0) as Format
                    }catch (e:java.lang.IndexOutOfBoundsException){
                        try{
                            video?.findAudioWithQuality(AudioQuality.low)?.get(0) as Format
                        }catch (e:java.lang.IndexOutOfBoundsException){
                            Log.i("YTDownloader",e.toString())
                            null
                        }
                    }
                }
                format?.let {
                    val url:String = format.url()
//                    Log.i("DHelper Link Found", url)
                    val outputFile:String = Environment.getExternalStorageDirectory().toString() + File.separator +
                            defaultDir + removeIllegalChars(type) + File.separator + (if(subFolder == null){""}else{ removeIllegalChars(subFolder)  + File.separator} + removeIllegalChars(track.name!!)+".m4a")

                    val downloadObject = DownloadObject(
                        track = track,
                        url = url,
                        outputDir = outputFile
                    )
                    Log.i("DH",outputFile)
                    startService(context!!, downloadObject)
                }
            }
        }
    }


    fun startService(context:Context,obj:DownloadObject? = null ) {
        val serviceIntent = Intent(context, ForegroundService::class.java)
        serviceIntent.putExtra("object",obj)
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
    @SuppressLint("SetJavaScriptEnabled")
    fun applyWebViewSettings(webView: WebView) {
        val desktopUserAgent =
            "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0"
        val mobileUserAgent =
            "Mozilla/5.0 (Linux; U; Android 4.4; en-us; Nexus 4 Build/JOP24G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"

        //Choose Mobile/Desktop client.
        webView.settings.userAgentString = desktopUserAgent
        webView.settings.loadWithOverviewMode = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.builtInZoomControls = true
        webView.settings.setSupportZoom(true)
        webView.isScrollbarFadingEnabled = false
        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webView.settings.displayZoomControls = false
        webView.settings.useWideViewPort = true
        webView.settings.javaScriptEnabled = true
        webView.settings.loadsImagesAutomatically = false
        webView.settings.blockNetworkImage = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.settings.safeBrowsingEnabled = true
        }
    }
}
data class YoutubeRequest(
    val spotifyFragment: SpotifyFragment? = null,
    val type:String,
    val subFolder:String?,
    val ytDownloader: YoutubeDownloader?,
    val searchQuery: String,
    val track: Track,
    val index: Int? = null
)