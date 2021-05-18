package com.shabinder.common.models

import kotlin.native.concurrent.AtomicReference

actual interface PlatformActions

actual val StubPlatformActions = object : PlatformActions {}

actual typealias NativeAtomicReference<T> = AtomicReference<T>
