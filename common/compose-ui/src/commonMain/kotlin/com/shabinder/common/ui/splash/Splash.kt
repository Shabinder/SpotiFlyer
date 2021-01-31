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

package com.shabinder.spotiflyer.ui.splash

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.ui.SpotiFlyerTypography
import com.shabinder.spotiflyer.ui.colorAccent
import com.shabinder.spotiflyer.ui.colorPrimary
import kotlinx.coroutines.delay

private const val SplashWaitTime: Long = 1100

@Composable
fun Splash(modifier: Modifier = Modifier, onTimeout: () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Adds composition consistency. Use the value when LaunchedEffect is first called
        val currentOnTimeout by rememberUpdatedState(onTimeout)

        LaunchedEffect(Unit) {
            delay(SplashWaitTime)
            currentOnTimeout()
        }
        Image(imageVector = vectorResource(id = R.drawable.ic_spotiflyer_logo))
        MadeInIndia(Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun MadeInIndia(
    modifier: Modifier = Modifier
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.made_with_love),
                color = colorPrimary,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.padding(start = 4.dp))
            Icon(vectorResource(id = R.drawable.ic_heart),tint = Color.Unspecified)
            Spacer(modifier = Modifier.padding(start = 4.dp))
            Text(
                text = stringResource(id = R.string.in_india),
                color = colorPrimary,
                fontSize = 22.sp
            )
        }
        Text(
            "by: Shabinder Singh",
            style = SpotiFlyerTypography.h6,
            color = colorAccent,
            fontSize = 14.sp
        )
    }
}