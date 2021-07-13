package com.shabinder.spotiflyer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.SystemSecurityUpdate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shabinder.common.translations.Strings
import com.shabinder.common.uikit.configurations.SpotiFlyerShapes
import com.shabinder.common.uikit.configurations.SpotiFlyerTypography
import com.shabinder.common.uikit.configurations.colorPrimary
import kotlinx.coroutines.delay

@ExperimentalAnimationApi
@Composable
fun PermissionDialog(
    permissionGranted: Boolean,
    requestStoragePermission: () -> Unit,
    disableDozeMode: () -> Unit,
) {
    var askForPermission by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(2600)
        askForPermission = true
    }

    AnimatedVisibility(
        askForPermission && !permissionGranted
    ) {
        AlertDialog(
            onDismissRequest = {},
            buttons = {
                TextButton(
                    {
                        requestStoragePermission()
                        disableDozeMode()
                    },
                    Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp).fillMaxWidth()
                        .background(colorPrimary, shape = SpotiFlyerShapes.medium)
                        .padding(horizontal = 8.dp),
                ) {
                    Text(Strings.grantPermissions(), color = Color.Black, fontSize = 18.sp, textAlign = TextAlign.Center)
                }
            },
            title = { Text(Strings.requiredPermissions(), style = SpotiFlyerTypography.h5, textAlign = TextAlign.Center) },
            backgroundColor = Color.DarkGray,
            text = {
                Column {
                    Spacer(modifier = Modifier.padding(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.SdStorage, Strings.storagePermission())
                        Spacer(modifier = Modifier.padding(start = 16.dp))
                        Column {
                            Text(
                                text = Strings.storagePermission(),
                                style = SpotiFlyerTypography.h6.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = Strings.storagePermissionReason(),
                                style = SpotiFlyerTypography.subtitle2,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.SystemSecurityUpdate, Strings.backgroundRunning())
                        Spacer(modifier = Modifier.padding(start = 16.dp))
                        Column {
                            Text(
                                text = Strings.backgroundRunning(),
                                style = SpotiFlyerTypography.h6.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = Strings.backgroundRunningReason(),
                                style = SpotiFlyerTypography.subtitle2,
                            )
                        }
                    }
                    /*Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Insights,"Analytics")
                        Spacer(modifier = Modifier.padding(start = 16.dp))
                        Column {
                            Text(
                                text = "Analytics",
                                style = SpotiFlyerTypography.h6.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Share Analytics Data (optional) with App Devs (Self-Hosted), It will never be used/shared/sold to any third party service.",
                                style = SpotiFlyerTypography.subtitle2,
                            )
                        }
                    }*/
                }
            }
        )
    }
}
