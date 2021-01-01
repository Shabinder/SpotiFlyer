package com.shabinder.spotiflyer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.compose.popUpTo
import com.shabinder.spotiflyer.ui.home.Home
import com.shabinder.spotiflyer.ui.tracklist.TrackList

@Composable
fun ComposeNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        //HomeScreen - Starting Point
        composable("home") {
            Home(navController = navController)
        }

        //Spotify Screen
        //Argument `link` = Link of Track/Album/Playlist
        composable(
            "track_list/{link}",
            arguments = listOf(navArgument("link") { type = NavType.StringType })
        ) {
            TrackList(
                fullLink = it.arguments?.getString("link") ?: "error",
                navController = navController
            )
        }
    }
}

fun NavController.navigateToTrackList(link:String, singleInstance: Boolean = true, inclusive:Boolean = false) {
    navigate("track_list/$link") {
        launchSingleTop = singleInstance
        popUpTo(route = "home") {
            this.inclusive = inclusive
        }
    }
}