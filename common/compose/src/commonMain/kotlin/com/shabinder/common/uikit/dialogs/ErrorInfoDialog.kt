package com.shabinder.common.uikit.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shabinder.common.models.Actions
import com.shabinder.common.translations.Strings
import com.shabinder.common.uikit.Dialog
import com.shabinder.common.uikit.configurations.SpotiFlyerTypography
import com.shabinder.common.uikit.configurations.colorAccent

typealias ErrorInfoDialogCallBacks = Pair<openAction, dismissAction>

@Composable
fun ErrorInfoDialog(error: Throwable): ErrorInfoDialogCallBacks {
    var isErrorDialogVisible by remember { mutableStateOf(false) }
    val onDismissDialog = { isErrorDialogVisible = false }
    val openErrorDialog = { isErrorDialogVisible = true }

    Dialog(isErrorDialogVisible, onDismissDialog) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color.Gray) // Gray
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    Strings.whatWentWrong(),
                    style = SpotiFlyerTypography.h5,
                    textAlign = TextAlign.Center,
                    color = colorAccent,
                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                )

                Spacer(Modifier.padding(top = 4.dp))
                Text(Strings.copyCodeInGithubIssue(), fontWeight = FontWeight.SemiBold)

                SelectionContainer(Modifier.padding(vertical = 8.dp).verticalScroll(rememberScrollState()).weight(1f)) {
                    Text(error.stackTraceToString(), fontWeight = FontWeight.Light)
                }
                Row(
                    Modifier.padding(top = 8.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = onDismissDialog, colors = ButtonDefaults.buttonColors()) {
                        Text(Strings.dismiss())
                    }
                    TextButton(onClick = { Actions.instance.copyToClipboard(error.stackTraceToString()) }, colors = ButtonDefaults.buttonColors()) {
                        Text(Strings.copyToClipboard())
                    }
                }
            }
        }
    }

    return ErrorInfoDialogCallBacks(openErrorDialog, onDismissDialog)
}
