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

package com.shabinder.spotiflyer.ui.youtube

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.model.formats.Format
import com.github.kiulian.downloader.model.quality.AudioQuality
import com.shabinder.spotiflyer.models.Artist
import com.shabinder.spotiflyer.models.Track
import kotlinx.coroutines.*

class YoutubeViewModel : ViewModel() {

    val ytTrack = MutableLiveData<Track>()
    val format = MutableLiveData<Format>()
    private val loading = "Loading"
    var title = MutableLiveData<String>().apply { value = "\"Loading!\"" }
    var coverUrl = MutableLiveData<String>().apply { value = loading }
    var ytDownloader: YoutubeDownloader? = null


    private var viewModelJob = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    fun getYTTrack(searchId:String) {
        uiScope.launch {
            withContext(Dispatchers.IO){
                Log.i("YT View Model",searchId)
                val video = ytDownloader?.getVideo(searchId)
                val detail = video?.details()
                val name = detail?.title()?.replace(detail.author()!!.toUpperCase(),"",true) ?: detail?.title()
                Log.i("YT View Model",detail.toString())
                ytTrack.postValue(
                    Track(
                    id = searchId,
                    name = name,
                    artists = listOf<Artist>(Artist(name = detail?.author())),
                    duration_ms = detail?.lengthSeconds()?.times(1000)?.toLong()?:0,
                    ytCoverUrl = "https://i.ytimg.com/vi/$searchId/maxresdefault.jpg"
                ))
                coverUrl.postValue("https://i.ytimg.com/vi/$searchId/maxresdefault.jpg")
                title.postValue(
                    if(name?.length!! > 17){"${name.subSequence(0,16)}..."}else{name}
                )
                format.postValue(try {
                    video?.findAudioWithQuality(AudioQuality.high)?.get(0) as Format
                } catch (e: IndexOutOfBoundsException) {
                    try {
                        video?.findAudioWithQuality(AudioQuality.medium)?.get(0) as Format
                    } catch (e: IndexOutOfBoundsException) {
                        try {
                            video?.findAudioWithQuality(AudioQuality.low)?.get(0) as Format
                        } catch (e: IndexOutOfBoundsException) {
                            Log.i("YTDownloader", e.toString())
                            null
                        }
                    }
                })
            }
        }
    }
}

