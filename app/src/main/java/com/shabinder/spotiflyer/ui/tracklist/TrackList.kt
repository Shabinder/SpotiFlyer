package com.shabinder.spotiflyer.ui.tracklist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.viewModel
import androidx.navigation.NavController
import com.bumptech.glide.RequestManager
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.models.PlatformQueryResult
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.ui.SpotiFlyerTypography
import com.shabinder.spotiflyer.ui.colorAccent
import dev.chrisbanes.accompanist.glide.GlideImage

/*
* UI for List of Tracks to be universally used.
* */
@Composable
fun TrackList(
    result: PlatformQueryResult,
    source: Source,
    modifier: Modifier = Modifier
){
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = {
            items(result.trackList) {
                TrackCard(track = it)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}


@Composable
fun TrackCard(track:TrackDetails) {
    Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        GlideImage(
            data = track.albumArtURL,
            loading = { Image(vectorResource(id = R.drawable.ic_song_placeholder)) },
            error = { Image(vectorResource(id = R.drawable.ic_musicplaceholder)) },
            contentScale = ContentScale.Inside,
            modifier = Modifier.preferredHeight(75.dp).preferredWidth(90.dp)
        )
        Column(modifier = Modifier.padding(horizontal = 8.dp).fillMaxHeight().weight(1f),verticalArrangement = Arrangement.SpaceBetween) {
            Text(track.title,maxLines = 1,overflow = TextOverflow.Ellipsis,style = SpotiFlyerTypography.h6,color = colorAccent)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(horizontal = 12.dp).fillMaxSize()
            ){
                Text("${track.artists.firstOrNull()}...",fontSize = 13.sp)
                Text("${track.durationSec/60} minutes, ${track.durationSec%60} sec",fontSize = 13.sp)
            }
        }
        Image(vectorResource(id = R.drawable.ic_arrow))
    }
}