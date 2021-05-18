package com.shabinder.common.di

import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toCValues
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.toNSData(): NSData = memScoped {
    return NSData.create(
        bytes = toCValues().getPointer(this),
        length = size.toULong()
    )
}

@OptIn(ExperimentalUnsignedTypes::class)
fun NSData.toByteArray(): ByteArray = memScoped {
    val size = length.toInt()
    val nsData = ByteArray(size)
    memcpy(nsData.refTo(0), bytes, size.toULong())
    return nsData
}
