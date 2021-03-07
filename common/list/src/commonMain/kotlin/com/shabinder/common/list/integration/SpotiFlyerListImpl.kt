package com.shabinder.common.list.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.shabinder.common.di.Picture
import com.shabinder.common.list.SpotiFlyerList
import com.shabinder.common.list.SpotiFlyerList.Dependencies
import com.shabinder.common.list.SpotiFlyerList.State
import com.shabinder.common.list.store.SpotiFlyerListStore.Intent
import com.shabinder.common.list.store.SpotiFlyerListStoreProvider
import com.shabinder.common.list.store.getStore
import com.shabinder.common.models.TrackDetails
import kotlinx.coroutines.flow.Flow

internal class SpotiFlyerListImpl(
    componentContext: ComponentContext,
    dependencies: Dependencies
): SpotiFlyerList,ComponentContext by componentContext, Dependencies by dependencies {

    private val store =
        instanceKeeper.getStore {
            SpotiFlyerListStoreProvider(
                dir = this.dir,
                storeFactory = storeFactory,
                fetchQuery = fetchQuery,
                downloadProgressFlow = downloadProgressFlow,
                link = link,
                showPopUpMessage = showPopUpMessage
            ).provide()
        }

    override val models: Flow<State> = store.states

    override fun onDownloadAllClicked(trackList: List<TrackDetails>) {
        store.accept(Intent.StartDownloadAll(trackList))
    }

    override fun onDownloadClicked(track:TrackDetails) {
        store.accept(Intent.StartDownload(track))
    }

    override fun onBackPressed(){
        listOutput.callback(SpotiFlyerList.Output.Finished)
    }

    override fun onRefreshTracksStatuses() {
        store.accept(Intent.RefreshTracksStatuses)
    }

    override suspend fun loadImage(url: String): Picture = dir.loadImage(url)
}