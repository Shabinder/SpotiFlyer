package com.shabinder.spotiflyer.ui.platforms.gaana

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import com.shabinder.spotiflyer.utils.*
import kotlinx.coroutines.launch

@Composable
fun Gaana(
    fullLink: String,
    navController: NavController
) {
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
        val result = gaanaSearch(
            type,
            link,
            sharedViewModel.gaanaInterface,
            sharedViewModel.databaseDAO,
        )

        log("Gaana",result.toString())
        log("Gaana Tracks",result.trackList.size.toString())



    }
}