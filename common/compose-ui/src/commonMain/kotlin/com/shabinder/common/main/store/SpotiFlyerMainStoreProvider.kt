package com.shabinder.common.main.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.shabinder.common.DownloadRecord
import com.shabinder.common.giveDonation
import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.main.SpotiFlyerMain.State
import com.shabinder.common.main.store.SpotiFlyerMainStore.Intent
import com.shabinder.common.openPlatform
import com.shabinder.common.shareApp
import com.shabinder.database.Database
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

internal class SpotiFlyerMainStoreProvider(
    private val storeFactory: StoreFactory,
    database: Database
) {

    fun provide(): SpotiFlyerMainStore =
        object : SpotiFlyerMainStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "SpotiFlyerHomeStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    val updates: Flow<List<DownloadRecord>> =
        database.downloadRecordDatabaseQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map {
                it.map { record ->
                    record.run{
                        DownloadRecord(id, type, name, link, coverUrl, totalFiles)
                    }
                }
            }


    private sealed class Result {
        data class ItemsLoaded(val items: List<DownloadRecord>) : Result()
        data class CategoryChanged(val category: SpotiFlyerMain.HomeCategory) : Result()
        data class LinkChanged(val link: String) : Result()
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Unit, State, Result, Nothing>() {
        override suspend fun executeAction(action: Unit, getState: () -> State) {
            updates.collect {
                dispatch(Result.ItemsLoaded(it))
            }
        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {//TODO: Add Dispatchers where needed
                is Intent.OpenPlatform -> openPlatform(intent.platformID, intent.platformLink)
                is Intent.GiveDonation -> giveDonation()
                is Intent.ShareApp -> shareApp()
                is Intent.SetLink -> dispatch(Result.LinkChanged(link = intent.link))
                is Intent.SelectCategory -> dispatch(Result.CategoryChanged(intent.category))
            }
        }
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
             when (result) {
                is Result.ItemsLoaded -> copy(records = result.items)
                is Result.LinkChanged -> copy(link = result.link)
                is Result.CategoryChanged -> copy(selectedCategory = result.category)
             }
    }
}