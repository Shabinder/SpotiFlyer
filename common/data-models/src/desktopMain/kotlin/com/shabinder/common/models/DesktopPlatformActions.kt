package com.shabinder.common.models

import kotlinx.coroutines.CoroutineScope

actual interface PlatformActions

internal actual val StubPlatformActions = object : PlatformActions {}

actual fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T = kotlinx.coroutines.runBlocking { block() }