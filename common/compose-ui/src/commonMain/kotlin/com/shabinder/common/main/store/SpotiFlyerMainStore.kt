package com.shabinder.common.main.store

import com.arkivanov.mvikotlin.core.store.Store
import com.shabinder.common.DownloadRecord
import com.shabinder.common.main.store.SpotiFlyerMainStore.*

internal interface SpotiFlyerMainStore: Store<Intent, State, Nothing> {
    sealed class Intent {
        data class OpenPlatform(val platformID:String,val platformLink:String):Intent()
        object GiveDonation : Intent()
        object ShareApp: Intent()
    }

    data class State(
        val records: List<DownloadRecord> = emptyList(),
        val link: String = ""
    )
}