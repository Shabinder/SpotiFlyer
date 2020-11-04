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
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.kiulian.downloader.model.formats.Format
import com.shabinder.spotiflyer.models.DownloadObject
import com.shabinder.spotiflyer.models.Track
import com.shabinder.spotiflyer.worker.ForegroundService
import java.io.File

object YTDownloadHelper {
    var context : Context? = null
    var statusBar: TextView? = null

    fun downloadFile(subFolder: String?, type: String,ytTrack: Track,format: Format?) {
        format?.let {
            val url:String = format.url()
//                    Log.i("DHelper Link Found", url)
            val outputFile:String = Environment.getExternalStorageDirectory().toString() + File.separator +
                    SpotifyDownloadHelper.defaultDir + SpotifyDownloadHelper.removeIllegalChars(type) + File.separator + (if(subFolder == null){""}else{ SpotifyDownloadHelper.removeIllegalChars(subFolder)  + File.separator} + SpotifyDownloadHelper.removeIllegalChars(
                ytTrack.name!!
            ) +".m4a")

            val downloadObject = DownloadObject(
                track = ytTrack,
                url = url,
                outputDir = outputFile
            )
            Log.i("DH",outputFile)
            startService(context!!, downloadObject)
            statusBar?.visibility= View.VISIBLE
        }
    }



    private fun startService(context:Context, obj: DownloadObject? = null ) {
        val serviceIntent = Intent(context, ForegroundService::class.java)
        serviceIntent.putExtra("object",obj)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

}