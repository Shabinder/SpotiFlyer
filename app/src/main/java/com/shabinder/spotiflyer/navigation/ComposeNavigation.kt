package com.shabinder.spotiflyer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.shabinder.spotiflyer.ui.home.Home
import com.shabinder.spotiflyer.ui.platforms.gaana.Gaana
import com.shabinder.spotiflyer.ui.platforms.spotify.Spotify
import com.shabinder.spotiflyer.ui.platforms.youtube.Youtube

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

        //Spotify Screen
        //Argument `link` = Link of Track/Album/Playlist
        composable(
            "spotify/{link}",
            arguments = listOf(navArgument("link") { type = NavType.StringType })
        ) {
            Spotify(
                link = it.arguments?.getString("link") ?: "error",
                navController = navController
            )
        }

        //Gaana Screen
        //Argument `link` = Link of Track/Album/Playlist
        composable(
            "gaana/{link}",
            arguments = listOf(navArgument("link") { type = NavType.StringType })
        ) {
            Gaana(
                link = it.arguments?.getString("link") ?: "error",
                navController = navController
            )
        }

        //Youtube Screen
        //Argument `link` = Link of Track/Album/Playlist
        composable(
            "youtube/{link}",
            arguments = listOf(navArgument("link") { type = NavType.StringType })
        ) {
            Youtube(
                link = it.arguments?.getString("link") ?: "error",
                navController = navController
            )
        }
    }
}