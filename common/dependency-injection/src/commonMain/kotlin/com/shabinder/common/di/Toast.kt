package com.shabinder.common.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

enum class ToastDuration(val value: Int) {
    Short(1000), Long(3000)
}

@Composable
expect fun Toast(
    text: String,
    visibility: MutableState<Boolean> = mutableStateOf(false),
    duration: ToastDuration = ToastDuration.Long
)