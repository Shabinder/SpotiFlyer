package com.shabinder.common.list.store

import com.arkivanov.mvikotlin.core.store.*
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.shabinder.common.FetchPlatformQueryResult
import com.shabinder.common.PlatformQueryResult
import com.shabinder.common.list.SpotiFlyerList.State
import com.shabinder.common.list.store.SpotiFlyerListStore.Intent
import com.shabinder.database.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class SpotiFlyerListStoreProvider(
    private val storeFactory: StoreFactory,
    private val database: Database,
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
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Unit, State, Result, Nothing>() {
        override suspend fun executeAction(action: Unit, getState: () -> State) {
            FetchPlatformQueryResult().query(link)?.let{
                dispatch(Result.ResultFetched(it))
            }
        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {//TODO: Add Dispatchers where needed
                is Intent.StartDownload -> {}//TODO()
                is Intent.SearchLink -> FetchPlatformQueryResult().query(link)?.let{
                    dispatch((Result.ResultFetched(it)))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
             when (result) {
                is Result.ResultFetched -> copy(result = result.result)
                is Result.SearchLink -> copy(link = result.link)
            }
    }
}