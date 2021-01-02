package com.shabinder.spotiflyer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.shabinder.spotiflyer.MainActivity
import com.shabinder.spotiflyer.providers.GaanaProvider
import com.shabinder.spotiflyer.providers.SpotifyProvider
import com.shabinder.spotiflyer.providers.YoutubeProvider
import com.shabinder.spotiflyer.ui.home.Home
import com.shabinder.spotiflyer.ui.tracklist.TrackList
import com.shabinder.spotiflyer.utils.sharedViewModel

@Composable
fun ComposeNavigation(
    mainActivity: MainActivity,
    navController: NavHostController,
    spotifyProvider: SpotifyProvider,
    gaanaProvider: GaanaProvider,
    youtubeProvider: YoutubeProvider
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        //HomeScreen - Starting Point
        composable("home") {
            Home(navController = navController, mainActivity)
        }

        //Spotify Screen
        //Argument `link` = Link of Track/Album/Playlist
        composable(
            "track_list/{link}",
            arguments = listOf(navArgument("link") { type = NavType.StringType })
        ) {
            TrackList(
                fullLink = it.arguments?.getString("link") ?: "error",
                navController = navController,
                spotifyProvider,
                gaanaProvider,
                youtubeProvider
            )
        }
    }
}

fun NavController.navigateToTrackList(link:String, singleInstance: Boolean = true, inclusive:Boolean = false) {
    sharedViewModel.updateLink(link)
    navigate("track_list/$link") {
        launchSingleTop = singleInstance
        popUpTo(route = "home") {
            this.inclusive = inclusive
        }
    }
}