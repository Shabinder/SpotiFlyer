package com.shabinder.common.main.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.main.SpotiFlyerMain.*
import com.shabinder.common.main.store.SpotiFlyerMainStore.Intent
import com.shabinder.common.main.store.SpotiFlyerMainStoreProvider
import com.shabinder.common.utils.getStore
import kotlinx.coroutines.flow.Flow

internal class SpotiFlyerMainImpl(
    componentContext: ComponentContext,
    dependencies: Dependencies
): SpotiFlyerMain,ComponentContext by componentContext, Dependencies by dependencies {

    private val store =
        instanceKeeper.getStore {
            SpotiFlyerMainStoreProvider(
                storeFactory = storeFactory,
                database = database
            ).provide()
        }

    override val models: Flow<State> = store.states

    override fun onLinkSearch(link: String) {
        mainOutput(Output.Search(link = link))
    }

    override fun onInputLinkChanged(link: String) {
        store.accept(Intent.SetLink(link))
    }
}