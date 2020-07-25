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

package com.shabinder.musicForEveryone.downloadHelper

import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.model.formats.Format
import com.github.kiulian.downloader.model.quality.AudioQuality
import com.shabinder.musicForEveryone.fragments.MainFragment
import com.shabinder.musicForEveryone.models.Track
import com.shabinder.musicForEveryone.utils.YoutubeInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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



interface DownloadHelper {

    /**
     * Function To Download All Tracks Available in a List
     **/
    suspend fun downloadAllTracks(trackList : List<Track>, ytDownloader: YoutubeDownloader?, downloadManager: DownloadManager?) {
        trackList.forEach { downloadTrack(null,ytDownloader,downloadManager,"${it.name} ${it.artists?.get(0)?.name ?:""}") }
    }


    suspend fun downloadTrack(
        mainFragment: MainFragment?,
        ytDownloader: YoutubeDownloader?,
        downloadManager: DownloadManager?,
        searchQuery: String
    ) {
        withContext(Dispatchers.IO) {
            val data = YoutubeInterface.search(searchQuery)?.get(0)
            if (data == null) {
                Log.i("DownloadHelper", "Youtube Request Failed!")
            } else {

                val video = ytDownloader?.getVideo(data.id)
                //Fetching a Video Object.
                val details = video?.details()
                try{
                    val format: Format =
                        video?.findAudioWithQuality(AudioQuality.medium)?.get(0) as Format
                    val audioUrl = format.url()
                    Log.i("DHelper Link Found", audioUrl)
                    if (audioUrl != null) {
                        downloadFile(audioUrl, downloadManager, details!!.title())
                        withContext(Dispatchers.Main){
                            mainFragment?.showToast("Download Started")
                        }
                    } else {
                        Log.i("YT audio url is null", format.toString())
                    }
                }catch (e:ArrayIndexOutOfBoundsException){
                    try{
                        val format: Format =
                            video?.findAudioWithQuality(AudioQuality.high)?.get(0) as Format
                        val audioUrl = format.url()
                        Log.i("DHelper Link Found", audioUrl)
                        if (audioUrl != null) {
                            downloadFile(audioUrl, downloadManager, details!!.title())
                            withContext(Dispatchers.Main){
                                mainFragment?.showToast("Download Started")
                            }
                        } else {
                            Log.i("YT audio url is null", format.toString())
                        }
                    }catch (e:ArrayIndexOutOfBoundsException){
                        try{
                            val format: Format =
                                video?.findAudioWithQuality(AudioQuality.high)?.get(0) as Format
                            val audioUrl = format.url()
                            Log.i("DHelper Link Found", audioUrl)
                            if (audioUrl != null) {
                                downloadFile(audioUrl, downloadManager, details!!.title())
                                withContext(Dispatchers.Main){
                                    mainFragment?.showToast("Download Started")
                                }
                            } else {
                                Log.i("YT audio url is null", format.toString())
                            }
                        }catch(e:ArrayIndexOutOfBoundsException){
                            Log.i("Catch",e.toString())
                        }
                    }
                }


            }
        }
    }


    /**
     * Downloading Using Android Download Manager
     * */
    suspend fun downloadFile(url: String, downloadManager: DownloadManager?, title: String) {
        withContext(Dispatchers.IO) {
            val audioUri = Uri.parse(url)
            val outputDir =
                File.separator + "Spotify-Downloads" + File.separator  + "${removeIllegalChars(title)}.mp3"

            val request = DownloadManager.Request(audioUri)
                .setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or
                            DownloadManager.Request.NETWORK_MOBILE
                )
                .setAllowedOverRoaming(false)
                .setTitle(title)
                .setDescription("Spotify Downloader Working Up here...")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, outputDir)
                .setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            downloadManager?.enqueue(request)
            Log.i("DownloadManager", "Download Request Sent")

        }
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
            ':'
        )
        var name = fileName
        for (c in illegalCharArray) {
            name = fileName.replace(c, '_')
        }
        return name
    }
}