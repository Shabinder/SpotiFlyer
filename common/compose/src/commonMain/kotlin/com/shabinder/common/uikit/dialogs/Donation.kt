package com.shabinder.common.uikit.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shabinder.common.uikit.DonationDialog

typealias DonationDialogCallBacks = Triple<openAction,dismissAction,snoozeAction>
private typealias openAction = () -> Unit
private typealias dismissAction = () -> Unit
private typealias snoozeAction = () -> Unit

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
    return DonationDialogCallBacks(openDonationDialog,dismissDonationDialog,snoozeDonationDialog)
}