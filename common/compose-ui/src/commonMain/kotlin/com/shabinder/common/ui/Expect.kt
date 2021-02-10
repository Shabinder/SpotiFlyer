package com.shabinder.common.ui

import kotlinx.coroutines.CoroutineDispatcher

expect fun showPopUpMessage(text: String)

expect val dispatcherIO: CoroutineDispatcher
