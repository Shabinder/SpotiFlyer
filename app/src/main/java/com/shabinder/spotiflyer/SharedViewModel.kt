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
import android.net.ConnectivityManager
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shabinder.spotiflyer.utils.SpotifyService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.File

class SharedViewModel : ViewModel() {
    var intentString = ""
    var spotifyService = MutableLiveData<SpotifyService>()
    var accessToken = MutableLiveData<String>().apply { value = "" }
    var isConnected = MutableLiveData<Boolean>().apply { value = false }
    val defaultDir = Environment.DIRECTORY_MUSIC + File.separator + "SpotiFlyer" + File.separator + ".Images" + File.separator


    private var viewModelJob = Job()

    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

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
    fun isOnline(context: Context): Boolean {
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }
}