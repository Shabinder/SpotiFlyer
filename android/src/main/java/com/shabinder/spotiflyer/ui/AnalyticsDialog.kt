package com.shabinder.spotiflyer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalAnimationApi::class)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Insights,"Analytics", Modifier.size(52.dp))
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Grant Analytics Access",style = SpotiFlyerTypography.h5,textAlign = TextAlign.Center)
                }
            },
            backgroundColor = Color.DarkGray,
            buttons = {
                TextButton(
                    {
                        dismissDialog()
                        enableAnalytics()
                    },
                    Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp).fillMaxWidth()
                        .background(colorPrimary, shape = SpotiFlyerShapes.medium)
                        .padding(horizontal = 8.dp),
                ) {
                    Text("Sure!",color = Color.Black,fontSize = 18.sp,textAlign = TextAlign.Center)
                }
            },
            text = {
                Text("Your Data is Anonymized and will never be shared with any 3rd party service",style = SpotiFlyerTypography.body2,textAlign = TextAlign.Center)
            },
            properties = DialogProperties(dismissOnBackPress = true,dismissOnClickOutside = false)
        )
    }   
}