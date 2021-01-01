package com.shabinder.spotiflyer.ui.tracklist

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.PlatformQueryResult
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.ui.SpotiFlyerTypography
import com.shabinder.spotiflyer.ui.colorAccent
import com.shabinder.spotiflyer.providers.queryGaana
import com.shabinder.spotiflyer.providers.querySpotify
import com.shabinder.spotiflyer.providers.queryYoutube
import com.shabinder.spotiflyer.ui.utils.calculateDominantColor
import com.shabinder.spotiflyer.utils.downloadTracks
import com.shabinder.spotiflyer.utils.loadAllImages
import com.shabinder.spotiflyer.utils.sharedViewModel
import com.shabinder.spotiflyer.utils.showDialog
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

    coroutineScope.launch {
        if(result == null){
            result = when{
                //SPOTIFY
                fullLink.contains("spotify",true) -> querySpotify(fullLink)

                //YOUTUBE
                fullLink.contains("youtube.com",true) || fullLink.contains("youtu.be",true) -> queryYoutube(fullLink)

                //GAANA
                fullLink.contains("gaana",true) -> queryGaana(fullLink)

                else -> {
                    showDialog("Link is Not Valid")
                    null
                }
            }
        }
        //Error Occurred And Has Been Shown to User
        if(result == null) navController.popBackStack()
    }


    result?.let{
        Box(modifier = modifier.fillMaxSize()){
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = {
                    item {
                        CoverImage(it.title,it.coverUrl,coroutineScope)
                    }
                    items(it.trackList) { item ->
                        TrackCard(
                            track = item,
                            onDownload = {
                                showDialog("Downloading ${it.title}")
                                downloadTracks(arrayListOf(it))
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
            DownloadAllButton(
                onClick = {
                    loadAllImages(
                        it.trackList.map { it.albumArtURL },
                        Source.Spotify
                    )
                    val finalList = it.trackList.filter{it.downloaded == DownloadStatus.NotDownloaded}
                    if (finalList.isNullOrEmpty()) showDialog("Not Downloading Any Song")
                    else downloadTracks(finalList as ArrayList<TrackDetails>)
                    for (track in it.trackList) {
                        if (track.downloaded == DownloadStatus.NotDownloaded) {
                            track.downloaded = DownloadStatus.Queued
                            //adapter.notifyItemChanged(viewModel.trackList.value!!.indexOf(track))
                        }
                    }
                    showDialog("Downloading All Tracks")
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
    onDownload:(TrackDetails)->Unit
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
        Image(vectorResource(id = R.drawable.ic_arrow), Modifier.clickable(onClick = { onDownload(track) }))
    }
}

suspend fun updateGradient(imageURL:String){
    calculateDominantColor(imageURL)?.color
        ?.let { sharedViewModel.updateGradientColor(it) }
}