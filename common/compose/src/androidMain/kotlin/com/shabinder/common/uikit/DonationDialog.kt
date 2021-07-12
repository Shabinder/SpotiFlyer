package com.shabinder.common.uikit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.shabinder.common.models.methods
import com.shabinder.common.translations.Strings

@OptIn(ExperimentalAnimationApi::class)
@Composable
actual fun DonationDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
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
                        Strings.supportUs(),
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
                                methods.value.openPlatform("", "https://opencollective.com/spotiflyer/donate")
                            }
                        )
                            .padding(vertical = 6.dp)
                    ) {
                        Icon(OpenCollectiveLogo(), "Open Collective Logo", Modifier.size(24.dp), tint = Color(0xFFCCCCCC))
                        Spacer(modifier = Modifier.padding(start = 16.dp))
                        Column {
                            Text(
                                text = "Open Collective",
                                style = SpotiFlyerTypography.h6
                            )
                            Text(
                                text = Strings.worldWideDonations(),
                                style = SpotiFlyerTypography.subtitle2
                            )
                        }
                    }
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
                        Icon(PaypalLogo(), "Paypal Logo", Modifier.size(24.dp), tint = Color(0xFFCCCCCC))
                        Spacer(modifier = Modifier.padding(start = 16.dp))
                        Column {
                            Text(
                                text = "Paypal",
                                style = SpotiFlyerTypography.h6
                            )
                            Text(
                                text = Strings.worldWideDonations(),
                                style = SpotiFlyerTypography.subtitle2
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                            .clickable(
                                onClick = {
                                    onDismiss()
                                    methods.value.giveDonation()
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(RazorPay(), "Indian Rupee Logo", Modifier.size(24.dp), tint = Color(0xFFCCCCCC))
                        Spacer(modifier = Modifier.padding(start = 16.dp))
                        Column {
                            Text(
                                text = "RazorPay",
                                style = SpotiFlyerTypography.h6
                            )
                            Text(
                                text = "${Strings.indianDonations()} (UPI / PayTM / PhonePe / Cards).",
                                style = SpotiFlyerTypography.subtitle2
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(vertical = 16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.padding(horizontal = 4.dp).fillMaxWidth()
                    ) {
                        OutlinedButton(onClick = onDismiss) {
                            Text(Strings.dismiss())
                        }
                        TextButton(onClick = onSnooze, colors = ButtonDefaults.buttonColors()) {
                            Text(Strings.remindLater())
                        }
                    }
                }
            }
        }
    }
}
