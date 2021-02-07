package com.shabinder.common.root

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.shabinder.common.ui.utils.verticalGradientScrim
import com.shabinder.common.list.SpotiFlyerListContent
import com.shabinder.common.main.SpotiFlyerMainContent
import com.shabinder.common.root.SpotiFlyerRoot.Child
import com.shabinder.common.ui.SpotiFlyerLogo
import com.shabinder.common.ui.appNameStyle

@Composable
fun SpotiFlyerRootContent(component: SpotiFlyerRoot) {
    val appBarColor = MaterialTheme.colors.surface.copy(alpha = 0.65f)
    Column(
        modifier = Modifier.fillMaxSize().verticalGradientScrim(
            //color = sharedViewModel.gradientColor.copy(alpha = 0.38f),
            color = appBarColor.copy(alpha = 0.38f),
            startYPercentage = 0.29f,
            endYPercentage = 0f,
        )
    ) {
        AppBar(
            backgroundColor = appBarColor,
            modifier = Modifier.fillMaxWidth()
        )
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
