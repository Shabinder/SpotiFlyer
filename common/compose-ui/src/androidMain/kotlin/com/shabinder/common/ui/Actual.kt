package com.shabinder.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.shabinder.common.database.appContext

@Composable
actual fun Toast(
    text: String,
    visibility: MutableState<Boolean>,
    duration: ToastDuration
){
    //We Have Android's Implementation of Toast so its just Empty
}

actual fun showPopUpMessage(text: String){
    android.widget.Toast.makeText(appContext,text, android.widget.Toast.LENGTH_SHORT).show()
}