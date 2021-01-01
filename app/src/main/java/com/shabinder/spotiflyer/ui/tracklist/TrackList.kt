package com.shabinder.spotiflyer.ui.tracklist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.models.PlatformQueryResult
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.ui.SpotiFlyerTypography
import com.shabinder.spotiflyer.ui.colorAccent
import com.shabinder.spotiflyer.ui.utils.calculateDominantColor
import com.shabinder.spotiflyer.utils.sharedViewModel
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/*
* UI for List of Tracks to be universally used.
**/
@Composable
fun TrackList(
    result: PlatformQueryResult,
    source: Source,
    modifier: Modifier = Modifier
){
    val coroutineScope = rememberCoroutineScope()
    Box(modifier = modifier.fillMaxSize()){
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = {
                item {
                    CoverImage(result.title,result.coverUrl,coroutineScope)
                }
                items(result.trackList) {
                    TrackCard(track = it)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
        DownloadAllButton(
            onClick = {},
            modifier = Modifier.padding(bottom = 24.dp).align(Alignment.BottomCenter)
        )
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
    scope.launch { updateGradient(coverURL) }
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
fun TrackCard(track:TrackDetails) {
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
        Image(vectorResource(id = R.drawable.ic_arrow))
    }
}

suspend fun updateGradient(imageURL:String){
    calculateDominantColor(imageURL)?.color
        ?.let { sharedViewModel.updateGradientColor(it) }
}