package com.shabinder.spotiflyer.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shabinder.common.di.isInternetAvailableState
import com.shabinder.common.uikit.SpotiFlyerShapes
import com.shabinder.common.uikit.SpotiFlyerTypography
import com.shabinder.common.uikit.colorOffWhite
import kotlinx.coroutines.delay

@ExperimentalAnimationApi
@Composable
fun NetworkDialog(
    networkAvailability: State<Boolean?> = isInternetAvailableState()
){
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit){
        delay(2600)
        visible = true
    }

    AnimatedVisibility(networkAvailability.value == false && visible){
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
                */},
            title = { Text("No Internet Connection!",
                style = SpotiFlyerTypography.h5,
                textAlign = TextAlign.Center) },
            backgroundColor = Color.DarkGray,
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally,verticalArrangement = Arrangement.Center){
                    Spacer(modifier = Modifier.padding(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Image(Icons.Rounded.CloudOff,"No Internet.",Modifier.size(42.dp),colorFilter = ColorFilter.tint(
                            colorOffWhite))
                        Spacer(modifier = Modifier.padding(start = 16.dp))
                        Text(
                            text = "Please Check Your Network Connection.",
                            style = SpotiFlyerTypography.subtitle1
                        )
                    }
                }
            }
            ,shape = SpotiFlyerShapes.medium
        )
    }
}