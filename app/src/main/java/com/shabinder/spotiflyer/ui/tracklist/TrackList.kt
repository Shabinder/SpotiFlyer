package com.shabinder.spotiflyer.ui.tracklist

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.PlatformQueryResult
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.ui.SpotiFlyerTypography
import com.shabinder.spotiflyer.ui.colorAccent
import com.shabinder.spotiflyer.providers.queryGaana
import com.shabinder.spotiflyer.providers.querySpotify
import com.shabinder.spotiflyer.providers.queryYoutube
import com.shabinder.spotiflyer.ui.utils.calculateDominantColor
import com.shabinder.spotiflyer.utils.downloadTracks
import com.shabinder.spotiflyer.utils.log
import com.shabinder.spotiflyer.utils.sharedViewModel
import com.shabinder.spotiflyer.utils.showDialog
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.*

/*
* UI for List of Tracks to be universally used.
**/
@Composable
fun TrackList(
    fullLink: String,
    navController: NavController,
    modifier: Modifier = Modifier
){
    val coroutineScope = rememberCoroutineScope()

    var result by remember(fullLink) { mutableStateOf<PlatformQueryResult?>(null) }

    coroutineScope.launch(Dispatchers.Default) {
        @Suppress("UnusedEquals")//Add Delay if result is not Initialized yet.
        try{result == null}catch(e:java.lang.IllegalStateException){delay(100)}
        if(result == null){
            result = when{
                /*
                * Using SharedViewModel's Link as NAVIGATION's Arg is buggy for links.
                * */
                //SPOTIFY
                sharedViewModel.link.contains("spotify",true) -> querySpotify(sharedViewModel.link)

                //YOUTUBE
                sharedViewModel.link.contains("youtube.com",true) || sharedViewModel.link.contains("youtu.be",true) -> queryYoutube(sharedViewModel.link)

                //GAANA
                sharedViewModel.link.contains("gaana",true) -> queryGaana(sharedViewModel.link)

                else -> {
                    showDialog("Link is Not Valid")
                    null
                }
            }
        }
        withContext(Dispatchers.Main){
            //Error Occurred And Has Been Shown to User
            if(result == null) navController.popBackStack()
        }
    }

    sharedViewModel.updateTrackList(result?.trackList ?: listOf())

    result?.let{
        Box(modifier = modifier.fillMaxSize()){
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = {
                    item {
                        CoverImage(it.title,it.coverUrl,coroutineScope)
                    }
                    itemsIndexed(sharedViewModel.trackList) { index, item ->
                        TrackCard(
                            track = item,
                            onDownload = {
                                downloadTracks(arrayListOf(item))
                                sharedViewModel.updateTrackStatus(index,DownloadStatus.Queued)
                            },
                        )
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
            DownloadAllButton(
                onClick = {
                    val finalList = sharedViewModel.trackList.filter{it.downloaded == DownloadStatus.NotDownloaded}
                    if (finalList.isNullOrEmpty()) showDialog("Not Downloading Any Song")
                    else downloadTracks(finalList as ArrayList<TrackDetails>)
                    for (track in sharedViewModel.trackList) {
                        if (track.downloaded == DownloadStatus.NotDownloaded) {
                            track.downloaded = DownloadStatus.Queued
                        }
                    }
                },
                modifier = Modifier.padding(bottom = 24.dp).align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun CoverImage(
    title: String,
    coverURL: String,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.padding(vertical = 8.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CoilImage(
            data = coverURL,
            contentScale = ContentScale.Crop,
            loading = { Image(vectorResource(id = R.drawable.ic_musicplaceholder)) },
            modifier = Modifier
                .preferredWidth(210.dp)
                .preferredHeight(230.dp)
                .clip(MaterialTheme.shapes.medium)
        )
        Text(
            text = title,
            style = SpotiFlyerTypography.h5,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            //color = colorAccent,
        )
    }
    scope.launch {
        updateGradient(coverURL)
    }
}

@Composable
fun DownloadAllButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    ExtendedFloatingActionButton(
        text = { Text("Download All") },
        onClick = onClick,
        icon = { Icon(imageVector = vectorResource(R.drawable.ic_download_arrow),tint = Color.Black) },
        backgroundColor = colorAccent,
        modifier = modifier
    )
}

@Composable
fun TrackCard(
    track:TrackDetails,
    onDownload:(TrackDetails)->Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        val imgUri = track.albumArtURL.toUri().buildUpon().scheme("https").build()
        CoilImage(
            data = imgUri,
            //Loading Placeholder Makes Scrolling very stuttery
//            loading = { Image(vectorResource(id = R.drawable.ic_song_placeholder)) },
            error = { Image(vectorResource(id = R.drawable.ic_musicplaceholder)) },
            contentScale = ContentScale.Inside,
//            fadeIn = true,
            modifier = Modifier.preferredHeight(75.dp).preferredWidth(90.dp)
        )
        Column(modifier = Modifier.padding(horizontal = 8.dp).preferredHeight(60.dp).weight(1f),verticalArrangement = Arrangement.SpaceEvenly) {
            Text(track.title,maxLines = 1,overflow = TextOverflow.Ellipsis,style = SpotiFlyerTypography.h6,color = colorAccent)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(horizontal = 8.dp).fillMaxSize()
            ){
                Text("${track.artists.firstOrNull()}...",fontSize = 13.sp)
                Text("${track.durationSec/60} minutes, ${track.durationSec%60} sec",fontSize = 13.sp)
            }
        }
        when(track.downloaded){
            DownloadStatus.Downloaded -> {
                Image(vectorResource(id = R.drawable.ic_tick))
            }
            DownloadStatus.Queued -> {
                CircularProgressIndicator()
            }
            DownloadStatus.Failed -> {
                Image(vectorResource(id = R.drawable.ic_error))
            }
            DownloadStatus.Downloading -> {
                CircularProgressIndicator(progress = track.progress.toFloat()/100f)
            }
            DownloadStatus.Converting -> {
                CircularProgressIndicator(progress = 100f,color = colorAccent)
            }
            DownloadStatus.NotDownloaded -> {
                Image(vectorResource(id = R.drawable.ic_arrow), Modifier.clickable(onClick = {
                    onDownload(track)
                }))
            }
        }
    }
}

suspend fun updateGradient(imageURL:String){
    calculateDominantColor(imageURL)?.color
        ?.let { sharedViewModel.updateGradientColor(it) }
}