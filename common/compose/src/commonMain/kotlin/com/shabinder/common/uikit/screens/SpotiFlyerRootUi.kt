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

@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.shabinder.common.uikit.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.animation.child.crossfadeScale
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.SpotiFlyerRoot.Child
import com.shabinder.common.translations.Strings
import com.shabinder.common.uikit.SpotiFlyerLogo
import com.shabinder.common.uikit.Toast
import com.shabinder.common.uikit.ToastDuration
import com.shabinder.common.uikit.configurations.appNameStyle
import com.shabinder.common.uikit.configurations.colorPrimaryDark
import com.shabinder.common.uikit.screens.splash.Splash
import com.shabinder.common.uikit.screens.splash.SplashState
import com.shabinder.common.uikit.utils.verticalGradientScrim

// Splash Status
private var isSplashShown = SplashState.Show

@Composable
fun SpotiFlyerRootContent(
    component: SpotiFlyerRoot,
    modifier: Modifier = Modifier,
    showSplash: Boolean = true
): SpotiFlyerRoot {
    isSplashShown = if (showSplash) SplashState.Show else SplashState.Completed
    val transitionState = remember { MutableTransitionState(isSplashShown) }
    val transition = updateTransition(transitionState, label = "transition")

    val splashAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 100) }, label = "Splash-Alpha"
    ) {
        if (it == SplashState.Show && isSplashShown == SplashState.Show) 1f else 0f
    }
    val contentAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 300) }, label = "Content-Alpha"
    ) {
        if (it == SplashState.Show && isSplashShown == SplashState.Show) 0f else 1f
    }
    val contentTopPadding by transition.animateDp(
        transitionSpec = { spring(stiffness = StiffnessLow) }, label = "Content-Padding"
    ) {
        if (it == SplashState.Show && isSplashShown == SplashState.Show) 100.dp else 0.dp
    }

    Box {
        Splash(
            modifier = modifier.alpha(splashAlpha),
            onTimeout = {
                transitionState.targetState = SplashState.Completed
                isSplashShown = SplashState.Completed
            }
        )
        MainScreen(
            modifier,
            contentAlpha,
            contentTopPadding,
            component
        )
        Toast(
            flow = component.toastState,
            duration = ToastDuration.Long
        )
    }
    return component
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    alpha: Float,
    topPadding: Dp = 0.dp,
    component: SpotiFlyerRoot
) {

    val appBarColor = MaterialTheme.colors.surface.copy(alpha = 0.65f)

    Column(
        modifier = Modifier.fillMaxSize()
            .alpha(alpha)
            .verticalGradientScrim(
                color = colorPrimaryDark.copy(alpha = 0.38f),
                startYPercentage = 0.29f,
                endYPercentage = 0f,
            ).then(modifier)
    ) {

        val activeComponent = component.routerState.subscribeAsState()
        val callBacks = component.callBacks
        AppBar(
            backgroundColor = appBarColor,
            onBackPressed = callBacks::popBackToHomeScreen,
            openPreferenceScreen = callBacks::openPreferenceScreen,
            isBackButtonVisible = activeComponent.value.activeChild.instance !is Child.Main,
            isSettingsIconVisible = activeComponent.value.activeChild.instance is Child.Main,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(top = topPadding))
        Children(
            routerState = component.routerState,
            animation = crossfadeScale()
        ) {
            when (val child = it.instance) {
                is Child.Main -> SpotiFlyerMainContent(component = child.component)
                is Child.List -> SpotiFlyerListContent(component = child.component)
                is Child.Preference -> SpotiFlyerPreferenceContent(component = child.component)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppBar(
    backgroundColor: Color,
    onBackPressed: () -> Unit,
    openPreferenceScreen: () -> Unit,
    isBackButtonVisible: Boolean,
    isSettingsIconVisible: Boolean,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        backgroundColor = backgroundColor,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(isBackButtonVisible) {
                    Icon(
                        Icons.Rounded.ArrowBackIosNew,
                        contentDescription = Strings.backButton(),
                        modifier = Modifier.clickable { onBackPressed() },
                        tint = Color.LightGray
                    )
                    Spacer(Modifier.padding(horizontal = 4.dp))
                }
                Image(
                    SpotiFlyerLogo(),
                    Strings.spotiflyerLogo(),
                    Modifier.requiredHeight(66.dp).requiredWidth(42.dp),
                    contentScale = ContentScale.FillHeight
                )
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text(
                    text = Strings.title(),
                    style = appNameStyle
                )
            }
        },
        actions = {
            AnimatedVisibility(isSettingsIconVisible) {
                IconButton(
                    onClick = { openPreferenceScreen() }
                ) {
                    Icon(Icons.Filled.Settings, Strings.preferences(), tint = Color.Gray)
                }
            }
        },
        modifier = modifier,
        elevation = 0.dp
    )
}
