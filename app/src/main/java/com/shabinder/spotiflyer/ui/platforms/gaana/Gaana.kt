package com.shabinder.spotiflyer.ui.platforms.gaana

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
import com.shabinder.spotiflyer.utils.*
import kotlinx.coroutines.launch

@Composable
fun Gaana(
    fullLink: String,
    navController: NavController
) {
    val source = Source.Gaana
    var result by remember { mutableStateOf<PlatformQueryResult?>(null) }

    //Coroutine Scope Active till this Composable is Active
    val coroutineScope = rememberCoroutineScope()

    //Link Schema: https://gaana.com/type/link
    val gaanaLink = fullLink.substringAfter("gaana.com/")

    val link = gaanaLink.substringAfterLast('/', "error")
    val type = gaanaLink.substringBeforeLast('/', "error").substringAfterLast('/')

    log("Gaana Fragment", "$type : $link")

    //Error
    if (type == "Error" || link == "Error"){
        showDialog("Please Check Your Link!")
        navController.popBackStack()
    }

    coroutineScope.launch {
        result = gaanaSearch(
            type,
            link,
            sharedViewModel.gaanaInterface,
            sharedViewModel.databaseDAO,
        )
    }
    result?.let {
        TrackList(
            result = it,
            source = source
        )
    }
}