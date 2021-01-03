/*
 * Copyright (c)  2021  Shabinder Singh
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
    youtubeProvider: YoutubeProvider,
    ) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        //HomeScreen - Starting Point
        composable("home") {
            Home(
                navController = navController,
                mainActivity,
            )
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