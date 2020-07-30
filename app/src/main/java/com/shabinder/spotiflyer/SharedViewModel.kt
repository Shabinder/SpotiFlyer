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

package com.shabinder.spotiflyer

import android.content.Context
import android.content.res.Resources
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.kiulian.downloader.YoutubeDownloader
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shabinder.spotiflyer.models.Album
import com.shabinder.spotiflyer.models.Playlist
import com.shabinder.spotiflyer.models.Track
import com.shabinder.spotiflyer.utils.SpotifyService
import com.shreyaspatil.EasyUpiPayment.EasyUpiPayment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.File

class SharedViewModel : ViewModel() {
    var intentString = ""
    var accessToken = MutableLiveData<String>().apply { value = "" }
    var spotifyService : SpotifyService? = null
    var ytDownloader : YoutubeDownloader? = null
    var isConnected = MutableLiveData<Boolean>().apply { value = false }
    var easyUpiPayment: EasyUpiPayment? = null
    val defaultDir = Environment.DIRECTORY_MUSIC + File.separator + "SpotiFlyer" + File.separator + ".Images" + File.separator


    private var viewModelJob = Job()

    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    suspend fun getTrackDetails(trackLink:String): Track?{
        return spotifyService?.getTrack(trackLink)
    }
    suspend fun getAlbumDetails(albumLink:String): Album?{
        return spotifyService?.getAlbum(albumLink)
    }
    suspend fun getPlaylistDetails(link:String): Playlist?{
        return spotifyService?.getPlaylist(link)
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

     fun showAlertDialog(resources:Resources,context: Context){
        MaterialAlertDialogBuilder(context,R.style.AlertDialogTheme)
            .setTitle(resources.getString(R.string.title))
            .setMessage(resources.getString(R.string.supporting_text))
            .setPositiveButton(resources.getString(R.string.cancel)) { _, _ ->
                // Respond to neutral button press
            }
            .show()
    }
}