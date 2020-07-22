package com.shabinder.musicForEveryone

import android.app.DownloadManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.musicForEveryone.utils.SpotifyNewService
import kaaes.spotify.webapi.android.SpotifyService
import kaaes.spotify.webapi.android.models.Album
import kaaes.spotify.webapi.android.models.Playlist
import kaaes.spotify.webapi.android.models.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class SharedViewModel : ViewModel() {
    var intentString = ""
    var accessToken:String = ""
    var userName = MutableLiveData<String>().apply { value = "Placeholder" }
    var spotify :SpotifyService? = null
    var spotifyExtra : SpotifyNewService? = null
    var ytDownloader : YoutubeDownloader? = null
    var downloadManager : DownloadManager? = null

    var viewModelJob = Job()

    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    suspend fun getTrackDetails(trackLink:String): Track?{
        return spotifyExtra?.getTrack(trackLink)
    }
    suspend fun getAlbumDetails(albumLink:String): Album?{
        return spotifyExtra?.getAlbum(albumLink)
    }
    suspend fun getPlaylistDetails(link:String): Playlist?{
        return spotifyExtra?.getPlaylist(link)
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}