package com.shabinder.musicForEveryone

import android.app.DownloadManager
import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.kiulian.downloader.YoutubeDownloader
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shabinder.musicForEveryone.models.Album
import com.shabinder.musicForEveryone.models.Playlist
import com.shabinder.musicForEveryone.models.Track
import com.shabinder.musicForEveryone.utils.SpotifyService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class SharedViewModel : ViewModel() {
    var intentString = ""
    var accessToken = MutableLiveData<String>().apply { value = "" }
    var spotifyService : SpotifyService? = null
    var ytDownloader : YoutubeDownloader? = null
    var downloadManager : DownloadManager? = null
    var isConnected = MutableLiveData<Boolean>().apply { value = false }

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
        val dialog = MaterialAlertDialogBuilder(context,R.style.AlertDialogTheme)
            .setTitle(resources.getString(R.string.title))
            .setMessage(resources.getString(R.string.supporting_text))
            .setPositiveButton(resources.getString(R.string.cancel)) { _, _ ->
                // Respond to neutral button press
            }
            .setBackground(resources.getDrawable(R.drawable.gradient))
            .show()
    }
}