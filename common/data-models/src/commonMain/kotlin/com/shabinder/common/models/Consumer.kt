package com.shabinder.common.models

/*
* Callback Utility
* */
interface Consumer<in T> {
    fun callback(value: T)
}

@Suppress("FunctionName") // Factory function
inline fun <T> Consumer(crossinline block: (T) -> Unit): Consumer<T> =
    object : Consumer<T> {
        override fun callback(value: T) {
            block(value)
        }
    }
