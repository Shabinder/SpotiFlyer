package com.shabinder.common.uikit.dialogs

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shabinder.common.models.Actions
import com.shabinder.common.translations.Strings
import com.shabinder.common.uikit.Dialog
import com.shabinder.common.uikit.OpenCollectiveLogo
import com.shabinder.common.uikit.PaypalLogo
import com.shabinder.common.uikit.RazorPay
import com.shabinder.common.uikit.configurations.SpotiFlyerTypography
import com.shabinder.common.uikit.configurations.colorAccent

typealias DonationDialogCallBacks = Triple<openAction, dismissAction, snoozeAction>
internal typealias openAction = () -> Unit
internal typealias dismissAction = () -> Unit
private typealias snoozeAction = () -> Unit

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DonationDialogComponent(onDismissExtra: () -> Unit): DonationDialogCallBacks {
    var isDonationDialogVisible by remember { mutableStateOf(false) }
    DonationDialog(
        isDonationDialogVisible,
        onSnooze = { isDonationDialogVisible = false },
        onDismiss = {
            isDonationDialogVisible = false
        }
    )

    val openDonationDialog = { isDonationDialogVisible = true }
    val snoozeDonationDialog = { isDonationDialogVisible = false }
    val dismissDonationDialog = {
        onDismissExtra()
        isDonationDialogVisible = false
    }
    return DonationDialogCallBacks(openDonationDialog, dismissDonationDialog, snoozeDonationDialog)
}

@ExperimentalAnimationApi
@Composable
fun DonationDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    Dialog(isVisible, onDismiss) {
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
                            Actions.instance.openPlatform("", "https://opencollective.com/spotiflyer/donate")
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
                            Actions.instance.openPlatform("", "https://www.paypal.com/paypalme/shabinder")
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
                                Actions.instance.giveDonation()
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
