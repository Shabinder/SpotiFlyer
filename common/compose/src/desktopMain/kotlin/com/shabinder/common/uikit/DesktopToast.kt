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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shabinder.common.uikit.configurations.SpotiFlyerTypography
import com.shabinder.common.uikit.configurations.colorOffWhite
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
actual fun Toast(
    flow: MutableStateFlow<String>,
    duration: ToastDuration
) {

    val state = flow.collectAsState("")
    val message = state.value

    AnimatedVisibility(
        visible = message != "",
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
        exit = slideOutHorizontally(targetOffsetX = { it / 4 }) + fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = 16.dp).padding(end = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                modifier = Modifier.sizeIn(maxWidth = 250.dp, maxHeight = 80.dp),
                color = Color(23, 23, 23),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, colorOffWhite)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = message,
                        color = Color(210, 210, 210),
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        style = SpotiFlyerTypography.body2,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                DisposableEffect(Unit) {
                    GlobalScope.launch {
                        delay(duration.value.toLong())
                        flow.value = ""
                    }
                    onDispose { }
                }
            }
        }
    }
}
