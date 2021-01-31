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

package com.shabinder.android

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Kermit
import com.shabinder.common.DownloadStatus
import com.shabinder.common.TrackDetails
import com.shabinder.common.YoutubeProvider
import com.shabinder.common.providers.GaanaProvider
import com.shabinder.common.providers.SpotifyProvider
import com.shabinder.database.Database
import com.shabinder.spotiflyer.ui.colorPrimaryDark
import com.tonyodev.fetch2.Status

class SharedViewModel(
    val database: Database,
    val logger: Kermit,
    val spotifyProvider: SpotifyProvider,
    val gaanaProvider : GaanaProvider,
    val youtubeProvider: YoutubeProvider
) : ViewModel() {
    var isAuthenticated by mutableStateOf(false)
        private set

    fun authenticated(s:Boolean) {
        isAuthenticated = s
    }

    /*
    * Nav Gives Error on YT links with ? sign
    * */
    var link by mutableStateOf("")
        private set

    fun updateLink(s:String) {
        link = s
    }


    val trackList = mutableStateListOf<TrackDetails>()

    fun updateTrackList(list:List<TrackDetails>){
        trackList.clear()
        trackList.addAll(list)
    }
    fun updateTrackStatus(position:Int, status: DownloadStatus){
        if(position != -1){
            val track = trackList[position].apply { downloaded = status }
            trackList[position] = track
        }
    }

    fun updateTrackStatus(intent: Intent){
        val trackDetails = intent.getSerializableExtra("track") as TrackDetails?
        trackDetails?.let {
            val position: Int =
                trackList.map { trackState -> trackState.title }.indexOf(it.title)
            logger.d{"$position, ${intent.action} , ${it.title}"}
            if (position != -1) {
                trackList.getOrNull(position)?.let{ track ->
                    when (intent.action) {
                        Status.QUEUED.name -> {
                            track.downloaded = DownloadStatus.Queued
                        }
                        Status.FAILED.name -> {
                            track.downloaded = DownloadStatus.Failed
                        }
                        Status.DOWNLOADING.name -> {
                            track.downloaded = DownloadStatus.Downloading
                        }
                        "Progress" -> {
                            //Progress Update
                            track.progress = intent.getIntExtra("progress", 0)
                            track.downloaded = DownloadStatus.Downloading
                        }
                        "Converting" -> {
                            //Progress Update
                            track.downloaded = DownloadStatus.Converting
                        }
                        "track_download_completed" -> {
                            track.downloaded = DownloadStatus.Downloaded
                        }
                    }
                    trackList[position] = track
                    logger.d{"TrackListUpdated"}
                }
            }
        }
    }

    var gradientColor by mutableStateOf(Color.Transparent)
    private set

    fun updateGradientColor(color: Color) {
        gradientColor = color
    }

    fun resetGradient() {
        gradientColor = colorPrimaryDark
    }
}