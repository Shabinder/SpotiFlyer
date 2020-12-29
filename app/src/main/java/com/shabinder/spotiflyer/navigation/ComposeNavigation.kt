package com.shabinder.spotiflyer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.shabinder.spotiflyer.home.Home
import com.shabinder.spotiflyer.tracklist.TrackList

@Composable
fun ComposeNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        //HomeScreen - Starting Point
        composable("home") {
            Home(navController = navController)
        }

        //Track list Screen
        //Argument `link` = Link of Track/Album/Playlist
        composable(
            "track_list/{link}",
            arguments = listOf(navArgument("link") { type = NavType.StringType })
        ) {
            TrackList(
                link = it.arguments?.getString("link") ?: "error",
                navController = navController
            )
        }
    }
}