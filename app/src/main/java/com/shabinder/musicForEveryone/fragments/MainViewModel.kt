package com.shabinder.musicForEveryone.fragments

import androidx.lifecycle.ViewModel
import com.shabinder.musicForEveryone.models.Track

class MainViewModel: ViewModel() {
    var searchLink:String = ""
    var trackList = mutableListOf<Track>()
    var coverUrl:String = ""

}