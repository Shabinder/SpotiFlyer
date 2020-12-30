package com.shabinder.spotiflyer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.compose.popUpTo
import com.shabinder.spotiflyer.ui.home.Home
import com.shabinder.spotiflyer.ui.platforms.gaana.Gaana
import com.shabinder.spotiflyer.ui.platforms.spotify.Spotify
import com.shabinder.spotiflyer.ui.platforms.youtube.Youtube
import com.shabinder.spotiflyer.utils.mainActivity
import com.shabinder.spotiflyer.utils.sharedViewModel
import com.shabinder.spotiflyer.utils.showDialog

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
            "spotify/{link}",
            arguments = listOf(navArgument("link") { type = NavType.StringType })
        ) {
            Spotify(
                fullLink = it.arguments?.getString("link") ?: "error",
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
                fullLink = it.arguments?.getString("link") ?: "error",
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
                fullLink = it.arguments?.getString("link") ?: "error",
                navController = navController
            )
        }
    }
}

fun NavController.navigateToPlatform(link:String){
    when{
        //SPOTIFY
        link.contains("spotify",true) -> {
            if(sharedViewModel.spotifyService.value == null){//Authentication pending!!
                mainActivity.authenticateSpotify()
            }
            this.navigateAndPopUpToHome("spotify/$link")
        }

        //YOUTUBE
        link.contains("youtube.com",true) || link.contains("youtu.be",true) -> {
            this.navigateAndPopUpToHome("youtube/$link")
        }

        //GAANA
        link.contains("gaana",true) -> {
            this.navigateAndPopUpToHome("gaana/$link")
        }

        else -> showDialog("Link is Not Valid")
    }
}

fun NavController.navigateAndPopUpToHome(route:String, inclusive:Boolean = false,singleInstance:Boolean = true){
    this.navigate(route){
        launchSingleTop = singleInstance
        popUpTo(route = "home"){
            this.inclusive = inclusive
        }
    }
}