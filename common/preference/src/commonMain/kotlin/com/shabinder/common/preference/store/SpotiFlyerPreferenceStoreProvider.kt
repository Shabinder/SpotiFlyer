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

package com.shabinder.common.preference.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.shabinder.common.di.preference.PreferenceManager
import com.shabinder.common.models.methods
import com.shabinder.common.preference.SpotiFlyerPreference.State
import com.shabinder.common.preference.store.SpotiFlyerPreferenceStore.Intent

internal class SpotiFlyerPreferenceStoreProvider(
    private val storeFactory: StoreFactory,
    private val preferenceManager: PreferenceManager
) {

    fun provide(): SpotiFlyerPreferenceStore =
        object :
            SpotiFlyerPreferenceStore,
            Store<Intent, State, Nothing> by storeFactory.create(
                name = "SpotiFlyerPreferenceStore",
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl
            ) {}

    private sealed class Result {
        data class ToggleAnalytics(val isEnabled: Boolean) : Result()
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Unit, State, Result, Nothing>() {
        override suspend fun executeAction(action: Unit, getState: () -> State) {
            dispatch(Result.ToggleAnalytics(preferenceManager.isAnalyticsEnabled))
        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                is Intent.OpenPlatform -> methods.value.openPlatform(intent.platformID, intent.platformLink)
                is Intent.GiveDonation -> methods.value.giveDonation()
                is Intent.ShareApp -> methods.value.shareApp()
                is Intent.ToggleAnalytics -> {
                    dispatch(Result.ToggleAnalytics(intent.enabled))
                    preferenceManager.toggleAnalytics(intent.enabled)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.ToggleAnalytics -> copy(isAnalyticsEnabled = result.isEnabled)
            }
    }
}
