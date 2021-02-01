package com.shabinder.common.list.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.shabinder.common.TrackDetails
import com.shabinder.common.list.SpotiFlyerList
import com.shabinder.common.list.SpotiFlyerList.Dependencies
import com.shabinder.common.list.SpotiFlyerList.State
import com.shabinder.common.list.store.SpotiFlyerListStore.Intent
import com.shabinder.common.list.store.SpotiFlyerListStoreProvider
import com.shabinder.common.utils.getStore
import kotlinx.coroutines.flow.Flow

internal class SpotiFlyerListImpl(
    componentContext: ComponentContext,
    dependencies: Dependencies
): SpotiFlyerList,ComponentContext by componentContext, Dependencies by dependencies {

    private val store =
        instanceKeeper.getStore {
            SpotiFlyerListStoreProvider(
                storeFactory = storeFactory,
                database = database,
                link = link
            ).provide()
        }

    override val models: Flow<State> = store.states

    override fun onDownloadClicked(trackList: List<TrackDetails>) {
        store.accept(Intent.StartDownload(trackList))
    }

    override fun onBackPressed(){
        listOutput(SpotiFlyerList.Output.Finished)
    }
}