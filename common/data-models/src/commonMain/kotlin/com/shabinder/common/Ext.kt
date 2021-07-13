package com.shabinder.common

fun <T : Any?> T?.requireNotNull(): T = requireNotNull(this)
