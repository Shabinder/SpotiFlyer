package com.shabinder.common.di

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/*
* Dependency Provider for IOS
* */
object IOSDeps : KoinComponent {
    val dir: Dir by inject() // = get()
    val fetchPlatformQueryResult: FetchPlatformQueryResult by inject() //  get()
    val database get() = dir.db
    val sharedFlow = DownloadProgressFlow
    val defaultDispatcher = dispatcherDefault
}
