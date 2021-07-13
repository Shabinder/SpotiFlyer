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

package com.shabinder.spotiflyer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shabinder.common.translations.Strings
import com.shabinder.common.uikit.configurations.SpotiFlyerShapes
import com.shabinder.common.uikit.configurations.SpotiFlyerTypography
import com.shabinder.common.uikit.configurations.colorOffWhite
import kotlinx.coroutines.delay

@ExperimentalAnimationApi
@Composable
fun NetworkDialog(
    networkAvailability: State<Boolean?>
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2600)
        visible = true
    }

    AnimatedVisibility(
        networkAvailability.value == false && visible
    ) {
        AlertDialog(
            onDismissRequest = {},
            buttons = {
                /*    TextButton({
                        //Retry Network Connection
                    },
                        Modifier.padding(bottom = 16.dp,start = 16.dp,end = 16.dp).fillMaxWidth().background(Color(0xFFFC5C7D),shape = RoundedCornerShape(size = 8.dp)).padding(horizontal = 8.dp),
                    ){
                        Text("Retry",color = Color.Black,fontSize = 18.sp,textAlign = TextAlign.Center)
                        Icon(Icons.Rounded.SyncProblem,"Check Network Connection Again")
                    }
                */
            },
            title = {
                Text(
                    Strings.noInternetConnection(),
                    style = SpotiFlyerTypography.h5,
                    textAlign = TextAlign.Center
                )
            },
            backgroundColor = Color.DarkGray,
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Spacer(modifier = Modifier.padding(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Image(
                            Icons.Rounded.CloudOff,
                            Strings.noInternetConnection(), Modifier.size(42.dp),
                            colorFilter = ColorFilter.tint(
                                colorOffWhite
                            )
                        )
                        Spacer(modifier = Modifier.padding(start = 16.dp))
                        Text(
                            text = Strings.checkInternetConnection(),
                            style = SpotiFlyerTypography.subtitle1
                        )
                    }
                }
            },
            shape = SpotiFlyerShapes.medium
        )
    }
}
