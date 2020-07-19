package com.shabinder.musicForEveryone

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.kiulian.downloader.YoutubeDownloader
import kaaes.spotify.webapi.android.SpotifyService
import kaaes.spotify.webapi.android.models.Album
import kaaes.spotify.webapi.android.models.Playlist
import kaaes.spotify.webapi.android.models.Track

class SharedViewModel : ViewModel() {
    var accessToken:String = ""
    var userName = MutableLiveData<String>().apply { value = "Placeholder" }
    var spotify :SpotifyService? = null
    var spotifyExtra :SpotifyNewService? = null
    var ytDownloader : YoutubeDownloader? = null

    fun getTrackDetails(trackLink:String): Track?{
        return spotify?.getTrack(trackLink)
    }
    fun getAlbumDetails(albumLink:String): Album?{
        return spotify?.getAlbum(albumLink)
    }
    fun getPlaylistDetails(link:String): Playlist?{
        return spotifyExtra?.getPlaylist(link)
    }

}