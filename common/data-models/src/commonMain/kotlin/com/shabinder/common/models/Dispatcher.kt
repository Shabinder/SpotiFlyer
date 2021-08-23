package com.shabinder.common.models

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

// IO-Dispatcher
expect val dispatcherIO: CoroutineDispatcher

// Default-Dispatcher
val dispatcherDefault: CoroutineDispatcher = Dispatchers.Default
