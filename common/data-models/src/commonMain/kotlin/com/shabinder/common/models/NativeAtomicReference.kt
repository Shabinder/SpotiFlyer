package com.shabinder.common.models

expect class NativeAtomicReference<T>(value: T) {
    var value: T
//    fun compareAndSet(expected: T, new: T): Boolean
}
