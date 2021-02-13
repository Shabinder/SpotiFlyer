package com.shabinder.common.root

import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.shabinder.common.list.SpotiFlyerListContent
import com.shabinder.common.main.SpotiFlyerMainContent
import com.shabinder.common.root.SpotiFlyerRoot.Child
import com.shabinder.common.ui.SpotiFlyerLogo
import com.shabinder.common.ui.appNameStyle
import com.shabinder.common.ui.splash.*

@Composable
fun SpotiFlyerRootContent(component: SpotiFlyerRoot):SpotiFlyerRoot {

    val transitionState = remember { MutableTransitionState(SplashState.Shown) }
    val transition = updateTransition(transitionState)

    val splashAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 100) }
    ) {
        if (it == SplashState.Shown) 1f else 0f
    }
    val contentAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 300) }
    ) {
        if (it == SplashState.Shown) 0f else 1f
    }
    val contentTopPadding by transition.animateDp(
        transitionSpec = { spring(stiffness = StiffnessLow) }
    ) {
        if (it == SplashState.Shown) 100.dp else 0.dp
    }

    Box{
        Splash(
            modifier = Modifier.alpha(splashAlpha),
            onTimeout = { transitionState.targetState = SplashState.Completed }
        )
        MainScreen(
            Modifier.alpha(contentAlpha),
            contentTopPadding,
            component
        )
    }
    return component
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, topPadding: Dp = 0.dp,component: SpotiFlyerRoot) {
    val appBarColor = MaterialTheme.colors.surface
    Column(
        modifier = modifier.fillMaxSize()
        /*.verticalGradientScrim(
        color = sharedViewModel.gradientColor.copy(alpha = 0.38f),
        color = appBarColor.copy(alpha = 0.38f),
        startYPercentage = 0.29f,
        endYPercentage = 0f,
    )*/
    ) {
        AppBar(
            backgroundColor = appBarColor,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(top = topPadding))
        Children(
            routerState = component.routerState,
            //TODO animation = crossfade()
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
        },
        actions = {
                IconButton(
                    onClick = {  /*TODO: Open Preferences*/ }
                ) {
                    Icon(Icons.Filled.Settings,"Preferences", tint = Color.Gray)
                }
        },
        modifier = modifier,
        elevation = 0.dp
    )
}
