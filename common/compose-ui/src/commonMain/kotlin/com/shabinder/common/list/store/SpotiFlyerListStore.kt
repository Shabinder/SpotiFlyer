package com.shabinder.common.list.store

import com.arkivanov.mvikotlin.core.store.Store
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.list.SpotiFlyerList.State
import com.shabinder.common.list.store.SpotiFlyerListStore.*

internal interface SpotiFlyerListStore: Store<Intent, State, Nothing> {
    sealed class Intent {
        data class SearchLink(val link: String): Intent()
        data class StartDownload(val track:TrackDetails): Intent()
        data class StartDownloadAll(val trackList: List<TrackDetails>): Intent()
    }
}
