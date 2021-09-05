package com.shabinder.common.models

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val dispatcherIO: CoroutineDispatcher = Dispatchers.Default