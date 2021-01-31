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

package com.shabinder.spotiflyer.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.jetcaster.util.verticalGradientScrim
import com.shabinder.spotiflyer.MainActivity
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.SharedViewModel
import com.shabinder.spotiflyer.navigation.ComposeNavigation
import com.shabinder.spotiflyer.ui.appNameStyle
import dev.chrisbanes.accompanist.insets.statusBarsHeight

@Composable
fun MainScreen(
    modifier: Modifier,
    mainActivity: MainActivity,
    sharedViewModel: SharedViewModel,
    navController: NavHostController,
    topPadding: Dp = 0.dp
){
    val appBarColor = MaterialTheme.colors.surface.copy(alpha = 0.65f)

    Column(
        modifier = modifier.fillMaxSize().verticalGradientScrim(
            color = sharedViewModel.gradientColor.copy(alpha = 0.38f),
            startYPercentage = 0.29f,
            endYPercentage = 0f,
        )
    ) {
        // Draw a scrim over the status bar which matches the app bar
        Spacer(
            Modifier.background(appBarColor).fillMaxWidth()
                .statusBarsHeight()
        )
        AppBar(
            backgroundColor = appBarColor,
            modifier = Modifier.fillMaxWidth()
        )
        //Space for Animation
        Spacer(Modifier.padding(top = topPadding))
        ComposeNavigation(
            mainActivity,
            navController,
            sharedViewModel.spotifyProvider,
            sharedViewModel.gaanaProvider,
            sharedViewModel.youtubeProvider
        )
    }
}

@Composable
fun AppBar(
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        backgroundColor = backgroundColor,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    imageVector = vectorResource(R.drawable.ic_spotiflyer_logo),
                    Modifier.preferredSize(32.dp)
                )
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text(
                    text = "SpotiFlyer",
                    style = appNameStyle
                )
            }
        },
        /*actions = {
            Providers(AmbientContentAlpha provides ContentAlpha.medium) {
                IconButton(
                    onClick = { *//* TODO: Open Preferences*//* }
                ) {
                    Icon(Icons.Filled.Settings, tint = Color.Gray)
                }
            }
        },*/
        modifier = modifier,
        elevation = 0.dp
    )
}
