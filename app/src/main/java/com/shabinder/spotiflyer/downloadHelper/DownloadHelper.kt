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
import androidx.core.content.ContextCompat
import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.model.formats.Format
import com.github.kiulian.downloader.model.quality.AudioQuality
import com.shabinder.spotiflyer.fragments.MainFragment
import com.shabinder.spotiflyer.models.DownloadObject
import com.shabinder.spotiflyer.models.Track
import com.shabinder.spotiflyer.utils.YoutubeInterface
import com.shabinder.spotiflyer.worker.ForegroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object DownloadHelper {

    var context : Context? = null
    val defaultDir = Environment.DIRECTORY_MUSIC + File.separator + "SpotiFlyer" + File.separator
    private var downloadList = arrayListOf<DownloadObject>()

    /**
     * Function To Download All Tracks Available in a List
     **/
    suspend fun downloadAllTracks(
        type:String,
        subFolder: String?,
        trackList: List<Track>, ytDownloader: YoutubeDownloader?) {
        var size = trackList.size
        trackList.forEach {
            size--
            if(size == 0){
                downloadTrack(null,type,subFolder,ytDownloader,"${it.name} ${it.artists?.get(0)?.name ?:""}", it ,0  )
            }else{
                downloadTrack(null,type,subFolder,ytDownloader,"${it.name} ${it.artists?.get(0)?.name ?:""}", it   )
            }
        }
    }

    suspend fun downloadTrack(
        mainFragment: MainFragment?,
        type:String,
        subFolder:String?,
        ytDownloader: YoutubeDownloader?,
        searchQuery: String,
        track: Track,
        index: Int? = null
    ) {
        withContext(Dispatchers.IO) {
            val data: YoutubeInterface.VideoItem = YoutubeInterface.search(searchQuery)?.get(0)!!

            //Fetching a Video Object.
            try {
                val audioUrl = getDownloadLink(AudioQuality.medium, ytDownloader, data)
                withContext(Dispatchers.Main) {
                    mainFragment?.showToast("Starting Download")
                }
                downloadFile(audioUrl, searchQuery, subFolder, type, track, index,mainFragment)
            } catch (e: java.lang.IndexOutOfBoundsException) {
                try {
                    val audioUrl = getDownloadLink(AudioQuality.high, ytDownloader, data)
                    withContext(Dispatchers.Main) {
                        mainFragment?.showToast("Starting Download")
                    }
                    downloadFile(audioUrl, searchQuery, subFolder, type, track, index,mainFragment)
                } catch (e: java.lang.IndexOutOfBoundsException) {
                    try {
                        val audioUrl = getDownloadLink(AudioQuality.low, ytDownloader, data)
                        withContext(Dispatchers.Main) {
                            mainFragment?.showToast("Starting Download")
                        }
                        downloadFile(audioUrl, searchQuery, subFolder, type, track, index,mainFragment)
                    } catch (e: java.lang.IndexOutOfBoundsException) {
                        Log.i("Catch", e.toString())
                    }
                }
            }

        }
    }


    private fun  getDownloadLink(quality: AudioQuality ,ytDownloader: YoutubeDownloader?,data:YoutubeInterface.VideoItem): String {
        val video = ytDownloader?.getVideo(data.id)
        val format: Format =
            video?.findAudioWithQuality(quality)?.get(0) as Format
        Log.i("Format", video.findAudioWithQuality(AudioQuality.medium)?.get(0)!!.mimeType())
        val audioUrl:String = format.url()
        Log.i("DHelper Link Found", audioUrl)
        return audioUrl
    }


    private suspend fun downloadFile(url: String, title: String, subFolder: String?, type: String, track:Track, index:Int? = null,mainFragment: MainFragment?) {
        withContext(Dispatchers.IO) {
            val outputFile:String = Environment.getExternalStorageDirectory().toString() + File.separator +
                    DownloadHelper.defaultDir + removeIllegalChars(type) + File.separator + (if(subFolder == null){""}else{ removeIllegalChars(subFolder)  + File.separator} + removeIllegalChars(track.name!!)+".m4a")

            if(!File(removeIllegalChars(outputFile.substringBeforeLast('.')) +".mp3").exists()){
                val downloadObject = DownloadObject(
                    track = track,
                    url = url,
                    outputDir = outputFile
                )
                Log.i("DH",outputFile)
                if(index==null){
                    downloadList.add(downloadObject)
                }else{
                    downloadList.add(downloadObject)
                    startService(context!!, downloadList)
                    downloadList = arrayListOf()
                }
            }else{withContext(Dispatchers.Main){mainFragment?.showToast("${track.name} is already Downloaded")}}
        }
    }


    private fun startService(context:Context,list: ArrayList<DownloadObject>) {
        val serviceIntent = Intent(context, ForegroundService::class.java)
        serviceIntent.putParcelableArrayListExtra("list",list)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    /**
     * Removing Illegal Chars from File Name
     * **/
    private fun removeIllegalChars(fileName: String): String? {
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
            ':',
            '-'
        )
        var name = fileName
        for (c in illegalCharArray) {
            name = fileName.replace(c, '_')
        }
        name = name.replace("\\s".toRegex(), "_")
        return name
    }
}