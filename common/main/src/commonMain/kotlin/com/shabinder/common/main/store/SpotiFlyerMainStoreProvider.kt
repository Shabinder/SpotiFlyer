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

package com.shabinder.common.main.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.main.SpotiFlyerMain.State
import com.shabinder.common.main.store.SpotiFlyerMainStore.Intent
import com.shabinder.common.models.DownloadRecord
import com.shabinder.common.models.Actions
import com.shabinder.common.utils.runOnMain
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

internal class SpotiFlyerMainStoreProvider(dependencies: SpotiFlyerMain.Dependencies): SpotiFlyerMain.Dependencies by dependencies {

    fun provide(): SpotiFlyerMainStore =
        object :
            SpotiFlyerMainStore,
            Store<Intent, State, Nothing> by storeFactory.create(
                name = "SpotiFlyerHomeStore",
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl
            ) {}

    val updates: Flow<List<DownloadRecord>>? =
        database?.downloadRecordDatabaseQueries
            ?.selectAll()
            ?.asFlow()
            ?.mapToList(Dispatchers.Default)
            ?.map {
                it.map { record ->
                    record.run {
                        DownloadRecord(id, type, name, link, coverUrl, totalFiles)
                    }
                }
            }

    private sealed class Result {
        data class ItemsLoaded(val items: List<DownloadRecord>) : Result()
        data class CategoryChanged(val category: SpotiFlyerMain.HomeCategory) : Result()
        data class LinkChanged(val link: String) : Result()
        data class AnalyticsToggled(val isEnabled: Boolean) : Result()
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Unit, State, Result, Nothing>() {
        override suspend fun executeAction(action: Unit, getState: () -> State) {
            dispatch(Result.AnalyticsToggled(preferenceManager.isAnalyticsEnabled))
            updates?.collect {
                dispatch(Result.ItemsLoaded(it))
            }
        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                is Intent.OpenPlatform -> Actions.instance.openPlatform(intent.platformID, intent.platformLink)
                is Intent.GiveDonation -> Actions.instance.giveDonation()
                is Intent.ShareApp -> Actions.instance.shareApp()
                is Intent.SetLink -> dispatch(Result.LinkChanged(link = intent.link))
                is Intent.SelectCategory -> dispatch(Result.CategoryChanged(intent.category))
                is Intent.ToggleAnalytics -> {
                    dispatch(Result.AnalyticsToggled(intent.enabled))
                    preferenceManager.toggleAnalytics(intent.enabled)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.ItemsLoaded -> copy(records = result.items)
                is Result.LinkChanged -> copy(link = result.link)
                is Result.CategoryChanged -> copy(selectedCategory = result.category)
                is Result.AnalyticsToggled -> copy(isAnalyticsEnabled = result.isEnabled)
            }
    }
}
