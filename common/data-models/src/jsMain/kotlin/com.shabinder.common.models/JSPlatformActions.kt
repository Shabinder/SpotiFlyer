package com.shabinder.common.models

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise


actual interface PlatformActions
internal actual val StubPlatformActions = object : PlatformActions {}
actual fun <T> runBlocking(block: suspend CoroutineScope.() -> T): dynamic = GlobalScope.promise(block = block)