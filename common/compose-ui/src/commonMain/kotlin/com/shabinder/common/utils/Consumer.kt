package com.shabinder.common.utils

/*
* Callback Utility
* */
interface Consumer<in T> {
    fun onCall(value: T)
}

@Suppress("FunctionName") // Factory function
inline fun <T> Consumer(crossinline block: (T) -> Unit): Consumer<T> =
    object : Consumer<T> {
        override fun onCall(value: T) {
            block(value)
        }
    }
