package com.shabinder.common.main.store

import com.arkivanov.mvikotlin.core.store.Store
import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.main.store.SpotiFlyerMainStore.Intent

internal interface SpotiFlyerMainStore: Store<Intent, SpotiFlyerMain.State, Nothing> {
    sealed class Intent {
        data class OpenPlatform(val platformID:String,val platformLink:String):Intent()
        data class SetLink(val link:String):Intent()
        data class SelectCategory(val category: SpotiFlyerMain.HomeCategory):Intent()
        object GiveDonation : Intent()
        object ShareApp: Intent()
    }
}
