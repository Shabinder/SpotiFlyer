package com.shabinder.musicForEveryone

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kaaes.spotify.webapi.android.SpotifyService

class MainViewModel : ViewModel() {

    var apiCame = MutableLiveData<Int>().apply { value = 0 }
    var userName = MutableLiveData<String>().apply { value = "Placeholder" }


    var spotify :SpotifyService? = null

}