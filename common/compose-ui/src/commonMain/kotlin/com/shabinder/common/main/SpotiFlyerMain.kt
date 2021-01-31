package com.shabinder.common.main

import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.badoo.reaktive.base.Consumer
import com.shabinder.common.DownloadRecord
import com.shabinder.database.Database

interface SpotiFlyerMain {

    val models: Value<Model>

    fun onDownloadRecordClicked(link: String)

    fun onInputLinkChanged(link: String)

    interface Dependencies {
        val storeFactory: StoreFactory
        val database: Database
        val mainOutput: Consumer<Output>
    }

    data class Model(
        val record: List<DownloadRecord>,
        val link: String
    )
    sealed class Output {
        data class Searched(val link: String) : Output()
    }
}