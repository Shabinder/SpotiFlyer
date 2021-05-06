package com.shabinder.common.uikit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shabinder.common.models.methods
import com.shabinder.common.uikit.PaypalLogo
import com.shabinder.common.uikit.RazorPay
import com.shabinder.common.uikit.SpotiFlyerShapes
import com.shabinder.common.uikit.SpotiFlyerTypography
import com.shabinder.common.uikit.colorAccent

@OptIn(ExperimentalAnimationApi::class)
@Composable
actual fun DonationDialog(
    isVisible:Boolean,
    onDismiss:()->Unit
){
    AnimatedVisibility(
        isVisible
    ) {

        Dialog(onDismiss) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color.Gray) // Gray
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Support Us",
                        style = SpotiFlyerTypography.h5,
                        textAlign = TextAlign.Center,
                        color = colorAccent,
                        modifier = Modifier
                    )
                    Spacer(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable(
                            onClick = {
                                onDismiss()
                                methods.value.openPlatform("", "https://www.paypal.com/paypalme/shabinder")
                            }
                        )
                            .padding(vertical = 6.dp)
                    ) {
                        Icon(PaypalLogo(), "Paypal Logo", tint = Color(0xFFCCCCCC))
                        Spacer(modifier = Modifier.padding(start = 16.dp))
                        Column {
                            Text(
                                text = "Paypal",
                                style = SpotiFlyerTypography.h6
                            )
                            Text(
                                text = "International Donations (Outside India).",
                                style = SpotiFlyerTypography.subtitle2
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                            .clickable(onClick = {
                                onDismiss()
                                methods.value.giveDonation()
                            }),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(RazorPay(), "Indian Rupee Logo", Modifier.size(32.dp), tint = Color(0xFFCCCCCC))
                        Spacer(modifier = Modifier.padding(start = 16.dp))
                        Column {
                            Text(
                                text = "RazorPay",
                                style = SpotiFlyerTypography.h6
                            )
                            Text(
                                text = "Indian Donations (UPI / PayTM / PhonePe / Cards).",
                                style = SpotiFlyerTypography.subtitle2
                            )
                        }
                    }
                }
            }
        }

        /*AlertDialog(
            buttons = {
                *//*    TextButton({
                        //Retry Network Connection
                    },
                        Modifier.padding(bottom = 16.dp,start = 16.dp,end = 16.dp).fillMaxWidth().background(Color(0xFFFC5C7D),shape = RoundedCornerShape(size = 8.dp)).padding(horizontal = 8.dp),
                    ){
                        Text("Retry",color = Color.Black,fontSize = 18.sp,textAlign = TextAlign.Center)
                        Icon(Icons.Rounded.SyncProblem,"Check Network Connection Again")
                    }
                *//*},
            *//*title = {
                    Column {
                        Text(
                            "Support Us",
                            style = SpotiFlyerTypography.h5,
                            textAlign = TextAlign.Center,
                            color = colorAccent,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.padding(vertical = 16.dp))
                    }
            },*//*
            backgroundColor = Color.DarkGray,
            text = {

            }
            ,shape = SpotiFlyerShapes.medium,
            onDismissRequest = onDismiss
        )*/
    }
}