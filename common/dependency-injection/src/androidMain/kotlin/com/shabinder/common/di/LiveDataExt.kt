package com.shabinder.common.di

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

@Composable
fun <T> LiveData<T>.observeAsState(): State<T?> = observeAsState(value)

/**
 * Starts observing this [LiveData] and represents its values via [State]. Every time there would
 * be new value posted into the [LiveData] the returned [State] will be updated causing
 * recomposition of every [State.value] usage.
 *
 * The inner observer will automatically be removed when this composable disposes or the current
 * [LifecycleOwner] moves to the [Lifecycle.State.DESTROYED] state.
 *
 * @sample androidx.compose.runtime.livedata.samples.LiveDataWithInitialSample
 */
@Composable
fun <R, T : R> LiveData<T>.observeAsState(initial: R): State<R> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember { mutableStateOf(initial) }
    DisposableEffect(this, lifecycleOwner) {
        val observer = Observer<T> { state.value = it }
        observe(lifecycleOwner, observer)
        onDispose { removeObserver(observer) }
    }
    return state
}