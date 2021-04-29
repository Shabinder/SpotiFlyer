/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.common.list.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.shabinder.common.database.getLogger
import com.shabinder.common.di.Dir
import com.shabinder.common.di.FetchPlatformQueryResult
import com.shabinder.common.di.downloadTracks
import com.shabinder.common.list.SpotiFlyerList.State
import com.shabinder.common.list.store.SpotiFlyerListStore.Intent
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.methods
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest

internal class SpotiFlyerListStoreProvider(
    private val dir: Dir,
    private val storeFactory: StoreFactory,
    private val fetchQuery: FetchPlatformQueryResult,
    private val link: String,
    private val downloadProgressFlow: MutableSharedFlow<HashMap<String, DownloadStatus>>
) {
    val logger = getLogger()
    fun provide(): SpotiFlyerListStore =
        object :
            SpotiFlyerListStore,
            Store<Intent, State, Nothing> by storeFactory.create(
                name = "SpotiFlyerListStore",
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl
            ) {}

    private sealed class Result {
        data class ResultFetched(val result: PlatformQueryResult, val trackList: List<TrackDetails>) : Result()
        data class UpdateTrackList(val list: List<TrackDetails>) : Result()
        data class UpdateTrackItem(val item: TrackDetails) : Result()
        data class ErrorOccurred(val error: Exception) : Result()
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Unit, State, Result, Nothing>() {

        override suspend fun executeAction(action: Unit, getState: () -> State) {
            executeIntent(Intent.SearchLink(link), getState)

            downloadProgressFlow.collectLatest { map ->
                logger.d(map.size.toString(), "ListStore: flow Updated")
                val updatedTrackList = getState().trackList.updateTracksStatuses(map)
                if (updatedTrackList.isNotEmpty()) dispatch(Result.UpdateTrackList(updatedTrackList))
            }
        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                is Intent.SearchLink -> {
                    try {
                        val result = fetchQuery.query(link)
                        if( result != null) {
                            result.trackList = result.trackList.toMutableList()
                            dispatch((Result.ResultFetched(result, result.trackList.updateTracksStatuses(downloadProgressFlow.replayCache.getOrElse(0) { hashMapOf() }))))
                            executeIntent(Intent.RefreshTracksStatuses, getState)
                        } else {
                            throw Exception("An Error Occurred, Check your Link / Connection")
                        }
                    } catch (e:Exception) {
                        dispatch(Result.ErrorOccurred(e))
                    }
                }

                is Intent.StartDownloadAll -> {
                    val finalList =
                        intent.trackList.filter { it.downloaded == DownloadStatus.NotDownloaded }
                    if (finalList.isNullOrEmpty()) methods.showPopUpMessage("All Songs are Processed")
                    else downloadTracks(finalList, fetchQuery, dir)

                    val list = intent.trackList.map {
                        if (it.downloaded == DownloadStatus.NotDownloaded)
                            return@map it.copy(downloaded = DownloadStatus.Queued)
                        it
                    }
                    dispatch(Result.UpdateTrackList(list.updateTracksStatuses(downloadProgressFlow.replayCache.getOrElse(0) { hashMapOf() })))
                }
                is Intent.StartDownload -> {
                    downloadTracks(listOf(intent.track), fetchQuery, dir)
                    dispatch(Result.UpdateTrackItem(intent.track.copy(downloaded = DownloadStatus.Queued)))
                }
                is Intent.RefreshTracksStatuses -> methods.queryActiveTracks()
            }
        }
    }
    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.ResultFetched -> copy(queryResult = result.result, trackList = result.trackList, link = link)
                is Result.UpdateTrackList -> copy(trackList = result.list)
                is Result.UpdateTrackItem -> updateTrackItem(result.item)
                is Result.ErrorOccurred -> copy(errorOccurred = result.error)
            }

        private fun State.updateTrackItem(item: TrackDetails): State {
            val position = this.trackList.map { it.title }.indexOf(item.title)
            if (position != -1) {
                return copy(trackList = trackList.toMutableList().apply { set(position, item) })
            }
            return this
        }
    }
    private fun List<TrackDetails>.updateTracksStatuses(map: HashMap<String, DownloadStatus>): List<TrackDetails> {
        val titleList = this.map { it.title }
        val updatedList = mutableListOf<TrackDetails>().also { it.addAll(this) }

        for (newTrack in map) {
            titleList.indexOf(newTrack.key).let { position ->
                if (position != -1) {
                    updatedList.getOrNull(position)?.copy(downloaded = newTrack.value, progress = (newTrack.value as? DownloadStatus.Downloading)?.progress ?: updatedList[position].progress)?.also { updatedTrack ->
                        updatedList[position] = updatedTrack
                        // logger.d("$position) ${updatedTrack.downloaded} - ${updatedTrack.title}","List Store Track Update")
                    }
                }
            }
        }
        return updatedList
    }
}
