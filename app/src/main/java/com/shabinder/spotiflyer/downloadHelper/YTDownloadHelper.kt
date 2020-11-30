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

import android.util.Log
import com.shabinder.spotiflyer.models.DownloadObject
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.utils.*
import com.shabinder.spotiflyer.utils.Provider.defaultDir
import com.shabinder.spotiflyer.utils.Provider.mainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

interface YTDownloadHelper {
    suspend fun downloadYTTracks(
        type:String,
        subFolder: String?,
        tracks:List<TrackDetails>,
    ){
        val downloadList = ArrayList<DownloadObject>()
        tracks.forEach {
            if(!isOnline()){
                showNoConnectionAlert()
                return
            }
            val outputFile: String = defaultDir +
                        removeIllegalChars(type) + File.separator +
                        (if (subFolder == null) { "" }
                        else { removeIllegalChars(subFolder) + File.separator }
                                + removeIllegalChars(it.title) + ".m4a")

            val downloadObject = DownloadObject(
                trackDetails = it,
                ytVideoId = it.albumArt.absolutePath.substringAfterLast("/")
                    .substringBeforeLast("."),
                outputFile = outputFile
            )

            downloadList.add(downloadObject)
        }
        Log.i("YT Downloader Helper","Download Request Sent")
        withContext(Dispatchers.Main){
            showMessage("Download Started, Now You can leave the App!")
            startService(mainActivity,downloadList)
        }
    }
}