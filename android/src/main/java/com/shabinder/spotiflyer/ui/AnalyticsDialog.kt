package com.shabinder.spotiflyer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.shabinder.common.uikit.SpotiFlyerShapes
import com.shabinder.common.uikit.SpotiFlyerTypography
import com.shabinder.common.uikit.colorPrimary

@ExperimentalAnimationApi
@Composable
fun AnalyticsDialog(
    isVisible:Boolean,
    enableAnalytics: ()->Unit,
    dismissDialog: () -> Unit,
) {
    // Analytics Permission Dialog
    AnimatedVisibility(isVisible) {
        AlertDialog(
            onDismissRequest = dismissDialog,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.SpaceEvenly) {
                    Icon(Icons.Rounded.Insights,"Analytics", Modifier.size(42.dp))
                    Spacer(Modifier.padding(horizontal = 8.dp))
                    Text("Grant Analytics",style = SpotiFlyerTypography.h5,textAlign = TextAlign.Center)
                }
            },
            backgroundColor = Color.DarkGray,
            buttons = {
                Column {
                    OutlinedButton(
                        onClick = dismissDialog,
                        Modifier.padding(horizontal = 8.dp).fillMaxWidth()
                            .background(Color.DarkGray, shape = SpotiFlyerShapes.medium)
                            .padding(horizontal = 8.dp),
                        shape = SpotiFlyerShapes.medium,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF303030))
                    ) {
                        Text("Nope",color = colorPrimary,fontSize = 18.sp,textAlign = TextAlign.Center)
                    }
                    Spacer(Modifier.padding(vertical = 4.dp))
                    TextButton(
                        onClick = {
                            dismissDialog()
                            enableAnalytics()
                        },
                        Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp).fillMaxWidth()
                            .background(colorPrimary, shape = SpotiFlyerShapes.medium)
                            .padding(horizontal = 8.dp),
                        shape = SpotiFlyerShapes.medium
                    ) {
                        Text("Sure",color = Color.Black,fontSize = 18.sp,textAlign = TextAlign.Center)
                    }
                }
            },
            text = {
                Text("Your Data is Anonymized and will never be shared with any 3rd party service",style = SpotiFlyerTypography.body2,textAlign = TextAlign.Center)
            },
            properties = DialogProperties(dismissOnBackPress = false,dismissOnClickOutside = false)
        )
    }   
}