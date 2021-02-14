package com.shabinder.common.list.store

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

internal class SpotiFlyerListStoreProvider(
    private val dir: Dir,
    private val storeFactory: StoreFactory,
    private val fetchQuery: FetchPlatformQueryResult,
    private val link: String
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
        data class ResultFetched(val result: PlatformQueryResult) : Result()
        data class SearchLink(val link: String) : Result()
        data class UpdateTrackList(val list:List<TrackDetails>): Result()
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Unit, State, Result, Nothing>() {
        override suspend fun executeAction(action: Unit, getState: () -> State) {
            fetchQuery.query(link)?.let{
                dispatch(Result.ResultFetched(it))
            }
        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {//TODO: Add Dispatchers where needed
                is Intent.SearchLink -> fetchQuery.query(link)?.let{
                    dispatch((Result.ResultFetched(it)))
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
                    dispatch(Result.UpdateTrackList(list))
                }
                is Intent.StartDownload -> {
                    val trackList = intent.wholeTrackList.toMutableList()
                    val track = trackList.getOrNull(intent.trackIndex)
                            ?.apply { downloaded = DownloadStatus.Queued }
                    track?.let {
                        trackList[intent.trackIndex] = it
                        dispatch(Result.UpdateTrackList(trackList))
                    }

                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
             when (result) {
                 is Result.ResultFetched -> copy(queryResult = result.result)
                 is Result.SearchLink -> copy(link = result.link)
                 is Result.UpdateTrackList -> copy(queryResult = this.queryResult?.apply { trackList = result.list })
             }
    }
}