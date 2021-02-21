package com.shabinder.common.list.store

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.arkivanov.mvikotlin.core.store.*
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.shabinder.common.di.Dir
import com.shabinder.common.di.FetchPlatformQueryResult
import com.shabinder.common.di.downloadTracks
import com.shabinder.common.list.SpotiFlyerList.State
import com.shabinder.common.list.store.SpotiFlyerListStore.Intent
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.ui.showPopUpMessage
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

internal class SpotiFlyerListStoreProvider(
    private val dir: Dir,
    private val storeFactory: StoreFactory,
    private val fetchQuery: FetchPlatformQueryResult,
    private val link: String,
    private val downloadProgressFlow: StateFlow<HashMap<String, DownloadStatus>>
) {
    fun provide(): SpotiFlyerListStore =
        object : SpotiFlyerListStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "SpotiFlyerListStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed class Result {
        data class ResultFetched(val result: PlatformQueryResult,val trackList: SnapshotStateList<TrackDetails>) : Result()
        data class UpdateTrackList(val list:SnapshotStateList<TrackDetails>): Result()
        data class UpdateTrackItem(val item:TrackDetails): Result()
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Unit, State, Result, Nothing>() {

        override suspend fun executeAction(action: Unit, getState: () -> State) {
            executeIntent(Intent.SearchLink(link),getState)

            downloadProgressFlow.collectLatest { map ->
                val updatedTrackList = getState().trackList.updateTracksStatuses(map)
                if(updatedTrackList.isNotEmpty()) dispatch(Result.UpdateTrackList(updatedTrackList))
            }
        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                is Intent.SearchLink -> fetchQuery.query(link)?.let{ result ->
                    result.trackList = result.trackList.toMutableList()
                    dispatch((Result.ResultFetched(result,result.trackList.toMutableList().updateTracksStatuses(downloadProgressFlow.value))))
                }

                is Intent.StartDownloadAll -> {
                    val finalList =
                        intent.trackList.filter { it.downloaded == DownloadStatus.NotDownloaded }
                    if (finalList.isNullOrEmpty())  showPopUpMessage("All Songs are Processed")
                    else downloadTracks(finalList,fetchQuery.youtubeMusic::getYTIDBestMatch,dir::saveFileWithMetadata)

                    val list = intent.trackList.map {
                        if (it.downloaded == DownloadStatus.NotDownloaded) {
                            it.downloaded = DownloadStatus.Queued
                        }
                        it
                    }
                    dispatch(Result.UpdateTrackList(list.toMutableList().updateTracksStatuses(downloadProgressFlow.value)))
                }

                is Intent.StartDownload -> {
                    dispatch(Result.UpdateTrackItem(intent.track.apply { downloaded = DownloadStatus.Queued }))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
             when (result) {
                 is Result.ResultFetched -> copy(queryResult = result.result, trackList = result.trackList ,link = link)
                 is Result.UpdateTrackList -> copy(trackList = result.list)
                 is Result.UpdateTrackItem -> updateTrackItem(result.item)
             }

        private fun State.updateTrackItem(item: TrackDetails):State{
            val position = this.trackList.map { it.title }.indexOf(item.title)
            if(position != -1){
                return copy(trackList = trackList.apply { set(position,item) })
            }
            return this
        }
    }
}

private fun MutableList<TrackDetails>.updateTracksStatuses(map:HashMap<String,DownloadStatus>):SnapshotStateList<TrackDetails>{
    val titleList = this.map { it.title }
    val newStateList = mutableStateListOf<TrackDetails>()
    for(newTrack in map){
        titleList.indexOf(newTrack.key).let { position ->
            this.getOrNull(position)?.apply { downloaded = newTrack.value }?.also { updatedTrack ->
                this[position] = updatedTrack
            }
        }
    }
    newStateList.addAll(this)
    return newStateList
}