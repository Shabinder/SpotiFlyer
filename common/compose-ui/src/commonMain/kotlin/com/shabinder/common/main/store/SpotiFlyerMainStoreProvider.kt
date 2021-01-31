package com.shabinder.common.main.store

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.ReaktiveExecutor
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapIterable
import com.badoo.reaktive.observable.observeOn
import com.badoo.reaktive.scheduler.mainScheduler
import com.shabinder.common.DownloadRecord
import com.shabinder.common.database.asObservable
import com.shabinder.common.main.store.SpotiFlyerMainStore.Intent
import com.shabinder.common.main.store.SpotiFlyerMainStore.State
import com.shabinder.database.Database
import com.squareup.sqldelight.Query

internal class SpotiFlyerMainStoreProvider(
    private val storeFactory: StoreFactory,
    private val database: Database
) {
    private sealed class Result {
        data class ItemsLoaded(val items: List<DownloadRecord>) : Result()
        data class TextChanged(val text: String) : Result()
    }

    private inner class ExecutorImpl : ReaktiveExecutor<Intent, Unit, State, Result, Nothing>() {
        override fun executeAction(action: Unit, getState: () -> State) {
            val updates: Observable<List<DownloadRecord>> =
                database.downloadRecordDatabaseQueries
                    .selectAll()
                    .asObservable(Query<com.shabinder.common.database.DownloadRecord>::executeAsList)
                    .mapIterable { it.run {
                        DownloadRecord(
                            id, type, name, link, coverUrl, totalFiles
                        )
                    } }


            updates
                .observeOn(mainScheduler)
                .map(Result::ItemsLoaded)
                .subscribeScoped(onNext = ::dispatch)
        }
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {//TODO
                is Intent.OpenPlatform -> {}
                is Intent.GiveDonation -> {}
                is Intent.ShareApp -> {}
            }
        }
    }
}