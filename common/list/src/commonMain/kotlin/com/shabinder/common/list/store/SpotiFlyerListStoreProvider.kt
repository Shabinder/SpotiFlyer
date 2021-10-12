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
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.shabinder.common.list.SpotiFlyerList
import com.shabinder.common.list.SpotiFlyerList.State
import com.shabinder.common.list.store.SpotiFlyerListStore.Intent
import com.shabinder.common.models.Actions
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.providers.downloadTracks
import com.shabinder.common.utils.runOnDefault
import com.shabinder.common.utils.runOnMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

internal class SpotiFlyerListStoreProvider(dependencies: SpotiFlyerList.Dependencies) :
    SpotiFlyerList.Dependencies by dependencies {
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
        data class ResultFetched(
            val result: PlatformQueryResult,
            val trackList: List<TrackDetails>
        ) : Result()

        data class UpdateTrackList(val list: List<TrackDetails>) : Result()
        data class UpdateTrackItem(val item: TrackDetails) : Result()
        data class ErrorOccurred(val error: Throwable) : Result()
        data class AskForSupport(val isAllowed: Boolean) : Result()
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Unit, State, Result, Nothing>() {

        override suspend fun executeAction(action: Unit, getState: () -> State) {
            executeIntent(Intent.SearchLink(link), getState)
            runOnDefault {
                fileManager.db?.downloadRecordDatabaseQueries?.getLastInsertId()
                    ?.executeAsOneOrNull()?.also {
                        // See if It's Time we can request for support for maintaining this project or not
                        fetchQuery.logger.d(
                            message = { "Database List Last ID: $it" },
                            tag = "Database Last ID"
                        )
                        val offset = preferenceManager.getDonationOffset
                        dispatchOnMain(
                            Result.AskForSupport(
                                // Every 3rd Interval or After some offset
                                isAllowed = offset < 4 && (it % offset == 0L)
                            )
                        )
                    }

                downloadProgressFlow.collect { map ->
                    // logger.d(map.size.toString(), "ListStore: flow Updated")
                    getState().trackList.updateTracksStatuses(map).also {
                        if (it.isNotEmpty())
                            dispatchOnMain(Result.UpdateTrackList(it))
                    }
                }
            }
        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            withContext(Dispatchers.Default) {
                when (intent) {
                    is Intent.SearchLink -> {
                        val resp = fetchQuery.query(link)
                        resp.fold(
                            success = { result ->
                                result.trackList =
                                    result.trackList.toMutableList()
                                        .updateTracksStatuses(
                                            downloadProgressFlow.replayCache.getOrElse(0) { hashMapOf() }
                                        )

                                dispatchOnMain(
                                    (Result.ResultFetched(
                                        result,
                                        result.trackList
                                    ))
                                )
                                executeIntent(Intent.RefreshTracksStatuses, getState)
                            },
                            failure = {
                                dispatchOnMain(Result.ErrorOccurred(it))
                            }
                        )
                    }

                    is Intent.StartDownloadAll -> {
                        val list = intent.trackList.map {
                            if (it.downloaded is DownloadStatus.NotDownloaded || it.downloaded is DownloadStatus.Failed)
                                return@map it.copy(downloaded = DownloadStatus.Queued)
                            it
                        }
                        dispatchOnMain(
                            Result.UpdateTrackList(
                                list.updateTracksStatuses(
                                    downloadProgressFlow.replayCache.getOrElse(
                                        0
                                    ) { hashMapOf() })
                            )
                        )

                        val finalList =
                            intent.trackList.filter { it.downloaded == DownloadStatus.NotDownloaded }
                        if (finalList.isEmpty()) Actions.instance.showPopUpMessage("All Songs are Processed")
                        else downloadTracks(finalList, fetchQuery, fileManager)
                    }

                    is Intent.StartDownload -> {
                        dispatchOnMain(Result.UpdateTrackItem(intent.track.copy(downloaded = DownloadStatus.Queued)))
                        downloadTracks(listOf(intent.track), fetchQuery, fileManager)
                    }

                    is Intent.RefreshTracksStatuses -> Actions.instance.queryActiveTracks()
                }
            }
        }

        private suspend fun dispatchOnMain(result: Result) = runOnMain { dispatch(result) }
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.ResultFetched -> copy(
                    queryResult = result.result,
                    trackList = result.trackList,
                    link = link
                )
                is Result.UpdateTrackList -> copy(trackList = result.list)
                is Result.UpdateTrackItem -> updateTrackItem(result.item)
                is Result.ErrorOccurred -> copy(errorOccurred = result.error)
                is Result.AskForSupport -> copy(askForDonation = result.isAllowed)
            }

        private fun State.updateTrackItem(item: TrackDetails): State {
            val position = this.trackList.map { it.title }.indexOf(item.title)
            if (position != -1) {
                return copy(trackList = trackList.toMutableList().apply { set(position, item) })
            }
            return this
        }
    }

    private fun List<TrackDetails>.updateTracksStatuses(map: Map<String, DownloadStatus>): List<TrackDetails> {
        // create a copy in order not to access real referenced ever-changing collections
        val trackList = ArrayList(this)
        val updatedMap = HashMap(map)

        repeat(trackList.size) { index ->
            trackList[index].also { oldTrack ->
                updatedMap[oldTrack.title]?.also { newStatus ->
                    trackList[index] = oldTrack.copy(downloaded = newStatus)
                }
            }
        }

        return trackList
    }
}
