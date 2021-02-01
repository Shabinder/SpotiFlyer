package com.shabinder.common.main.store

import com.arkivanov.mvikotlin.core.store.Store
import com.shabinder.common.DownloadRecord
import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.main.store.SpotiFlyerMainStore.*

internal interface SpotiFlyerMainStore: Store<Intent, SpotiFlyerMain.State, Nothing> {
    sealed class Intent {
        data class OpenPlatform(val platformID:String,val platformLink:String):Intent()
        data class SetLink(val link:String):Intent()
        object GiveDonation : Intent()
        object ShareApp: Intent()
    }
}
