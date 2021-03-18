/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.common.uikit

import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.SpotiFlyerRoot.Child
import com.shabinder.common.uikit.splash.Splash
import com.shabinder.common.uikit.splash.SplashState
import com.shabinder.common.uikit.utils.verticalGradientScrim

// To Not Show Splash Again After Configuration Change in Android
private var isSplashShown = SplashState.Shown

@Composable
fun SpotiFlyerRootContent(component: SpotiFlyerRoot, statusBarHeight:Dp = 0.dp): SpotiFlyerRoot {

    val transitionState = remember { MutableTransitionState(SplashState.Shown) }
    val transition = updateTransition(transitionState)

    val splashAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 100) }
    ) {
        if (it == SplashState.Shown && isSplashShown == SplashState.Shown) 1f else 0f
    }
    val contentAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 300) }
    ) {
        if (it == SplashState.Shown && isSplashShown == SplashState.Shown) 0f else 1f
    }
    val contentTopPadding by transition.animateDp(
        transitionSpec = { spring(stiffness = StiffnessLow) }
    ) {
        if (it == SplashState.Shown  && isSplashShown == SplashState.Shown) 100.dp else 0.dp
    }

    Box{
        Splash(
            modifier = Modifier.alpha(splashAlpha),
            onTimeout = {
                transitionState.targetState = SplashState.Completed
                isSplashShown = SplashState.Completed
            }
        )
        MainScreen(
            Modifier.alpha(contentAlpha),
            contentTopPadding,
            statusBarHeight,
            component
        )
    }
    return component
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, topPadding: Dp = 0.dp,statusBarHeight: Dp = 0.dp,component: SpotiFlyerRoot) {

    val appBarColor = MaterialTheme.colors.surface.copy(alpha = 0.65f)

    Column(
        modifier = modifier.fillMaxSize()
        .verticalGradientScrim(
        color = colorPrimaryDark.copy(alpha = 0.38f),
        startYPercentage = 0.29f,
        endYPercentage = 0f,
    )
    ) {
        Spacer(Modifier.background(appBarColor).height(statusBarHeight).fillMaxWidth())
        LocalViewConfiguration.current

        AppBar(
            backgroundColor = appBarColor,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(top = topPadding))
        Children(
            routerState = component.routerState,
        ) { child, _ ->
            when (child) {
                is Child.Main -> SpotiFlyerMainContent(component = child.component)
                is Child.List -> SpotiFlyerListContent(component = child.component)
            }
        }
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
                    imageVector = SpotiFlyerLogo(),
                    "SpotiFlyer Logo",
                    Modifier.size(32.dp),
                )
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text(
                    text = "SpotiFlyer",
                    style = appNameStyle
                )
            }
        },/*
        actions = {
                IconButton(
                    onClick = {  *//*TODO: Open Preferences*//* }
                ) {
                    Icon(Icons.Filled.Settings,"Preferences", tint = Color.Gray)
                }
        },*/
        modifier = modifier,
        elevation = 0.dp
    )
}
