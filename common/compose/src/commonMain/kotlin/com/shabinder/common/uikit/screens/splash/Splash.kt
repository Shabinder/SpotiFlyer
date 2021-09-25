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

package com.shabinder.common.uikit.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shabinder.common.translations.Strings
import com.shabinder.common.uikit.HeartIcon
import com.shabinder.common.uikit.SpotiFlyerLogo
import com.shabinder.common.uikit.configurations.SpotiFlyerTypography
import com.shabinder.common.uikit.configurations.colorAccent
import com.shabinder.common.uikit.configurations.colorPrimary
import kotlinx.coroutines.delay

private const val SplashWaitTime: Long = 2000
enum class SplashState { Show, Completed }

@Composable
fun Splash(modifier: Modifier = Modifier, onTimeout: () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Adds composition consistency. Use the value when LaunchedEffect is first called
        val currentOnTimeout by rememberUpdatedState(onTimeout)

        LaunchedEffect(Unit) {
            delay(SplashWaitTime)
            currentOnTimeout()
        }

        Image(SpotiFlyerLogo(), Strings.spotiflyerLogo(),modifier = Modifier.fillMaxSize())
        MadeInIndia(Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun MadeInIndia(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${Strings.madeWith()} ",
                color = colorPrimary,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.padding(start = 4.dp))
            Icon(HeartIcon(), Strings.love(), tint = Color.Unspecified)
            Spacer(modifier = Modifier.padding(start = 4.dp))
            Text(
                text = " ${Strings.inIndia()}",
                color = colorPrimary,
                fontSize = 22.sp
            )
        }
        Text(
            Strings.byDeveloperName(),
            style = SpotiFlyerTypography.h6,
            color = colorAccent,
            fontSize = 14.sp
        )
    }
}
