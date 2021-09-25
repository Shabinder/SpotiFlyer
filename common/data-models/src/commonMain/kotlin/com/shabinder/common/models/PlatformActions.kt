package com.shabinder.common.models

import kotlinx.coroutines.CoroutineScope

expect interface PlatformActions

internal expect val StubPlatformActions: PlatformActions

expect fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T