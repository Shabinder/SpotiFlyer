package com.shabinder.android.di

import com.shabinder.common.database.appContext
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import org.koin.dsl.module

val appModule = module {
    single { createFetchInstance() }
}

private fun createFetchInstance():Fetch{
    val fetchConfiguration =
        FetchConfiguration.Builder(appContext).run {
            setNamespace("ForegroundDownloaderService")
            setDownloadConcurrentLimit(4)
            build()
        }

    return Fetch.run {
        setDefaultInstanceConfiguration(fetchConfiguration)
        getDefaultInstance()
    }.apply {
        removeAll() //Starting fresh
    }
}