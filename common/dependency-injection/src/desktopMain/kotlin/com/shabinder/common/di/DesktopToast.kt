package com.shabinder.common.di

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

actual val dispatcherIO = Dispatchers.IO

private val message: MutableState<String> = mutableStateOf("")
private val state: MutableState<Boolean> = mutableStateOf(false)

actual fun showPopUpMessage(text: String) {
    message.value = text
    state.value = true
}

private var isShown: Boolean = false

@Composable
actual fun Toast(
    text: String,
    visibility: MutableState<Boolean>,
    duration: ToastDuration
) {
    if (isShown) {
        return
    }

    if (visibility.value) {
        isShown = true
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = 20.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier.size(300.dp, 70.dp),
                color = Color(23, 23, 23),
                shape = RoundedCornerShape(4.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = text,
                        color = Color(210, 210, 210)
                    )
                }
                DisposableEffect(Unit) {
                    GlobalScope.launch {
                        delay(duration.value.toLong())
                        isShown = false
                        visibility.value = false
                    }
                    onDispose {  }
                }
            }
        }
    }
}