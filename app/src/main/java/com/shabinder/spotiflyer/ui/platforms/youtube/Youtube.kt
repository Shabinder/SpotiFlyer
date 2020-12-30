package com.shabinder.spotiflyer.ui.platforms.youtube

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import com.shabinder.spotiflyer.models.PlatformQueryResult
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.ui.tracklist.TrackList
import com.shabinder.spotiflyer.utils.sharedViewModel
import com.shabinder.spotiflyer.utils.showDialog
import kotlinx.coroutines.launch


private const val sampleDomain2 = "youtu.be"
private const val sampleDomain1 = "youtube.com"

@Composable
fun Youtube(fullLink: String, navController: NavController,) {
    val source = Source.YouTube
    var result by remember { mutableStateOf<PlatformQueryResult?>(null) }
    //Coroutine Scope Active till this Composable is Active
    val coroutineScope = rememberCoroutineScope()

    coroutineScope.launch {
        val link = fullLink.removePrefix("https://").removePrefix("http://")
        if(link.contains("playlist",true) || link.contains("list",true)){
            // Given Link is of a Playlist
            val playlistId = link.substringAfter("?list=").substringAfter("&list=").substringBefore("&")
            getYTPlaylist(
                playlistId,
                sharedViewModel.ytDownloader,
                sharedViewModel.databaseDAO
            )
        }else{//Given Link is of a Video
            var searchId = "error"
            if(link.contains(sampleDomain1,true) ){
                searchId =  link.substringAfterLast("=","error")
            }
            if(link.contains(sampleDomain2,true) ){
                searchId = link.substringAfterLast("/","error")
            }
            if(searchId != "error") {
                result = getYTTrack(
                    searchId,
                    sharedViewModel.ytDownloader,
                    sharedViewModel.databaseDAO
                )
            }else{
                showDialog("Your Youtube Link is not of a Video!!")
                navController.popBackStack()
            }
        }
    }
    result?.let {
        TrackList(
            result = it,
            source = source
        )
    }
}