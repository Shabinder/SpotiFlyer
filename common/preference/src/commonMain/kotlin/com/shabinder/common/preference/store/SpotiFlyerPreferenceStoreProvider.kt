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
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.Actions
import com.shabinder.common.models.spotify.SpotifyCredentials
import com.shabinder.common.preference.SpotiFlyerPreference
import com.shabinder.common.preference.SpotiFlyerPreference.State
import com.shabinder.common.preference.store.SpotiFlyerPreferenceStore.Intent

internal class SpotiFlyerPreferenceStoreProvider(
    dependencies: SpotiFlyerPreference.Dependencies
) : SpotiFlyerPreference.Dependencies by dependencies {

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
        data class AnalyticsToggled(val isEnabled: Boolean) : Result()
        data class DownloadPathSet(val path: String) : Result()
        data class PreferredAudioQualityChanged(val quality: AudioQuality) : Result()
        data class SpotifyCredentialsUpdated(val spotifyCredentials: SpotifyCredentials) : Result()
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Unit, State, Result, Nothing>() {
        override suspend fun executeAction(action: Unit, getState: () -> State) {
            dispatch(Result.AnalyticsToggled(preferenceManager.isAnalyticsEnabled))
            dispatch(Result.PreferredAudioQualityChanged(preferenceManager.audioQuality))
            dispatch(Result.SpotifyCredentialsUpdated(preferenceManager.spotifyCredentials))
            dispatch(Result.DownloadPathSet(fileManager.defaultDir()))
        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                is Intent.OpenPlatform -> Actions.instance.openPlatform(intent.platformID, intent.platformLink)
                is Intent.GiveDonation -> Actions.instance.giveDonation()
                is Intent.ShareApp -> Actions.instance.shareApp()
                is Intent.ToggleAnalytics -> {
                    dispatch(Result.AnalyticsToggled(intent.enabled))
                    preferenceManager.toggleAnalytics(intent.enabled)
                }
                is Intent.SetDownloadDirectory -> {
                    dispatch(Result.DownloadPathSet(intent.path))
                    preferenceManager.setDownloadDirectory(intent.path)
                }
                is Intent.SetPreferredAudioQuality -> {
                    dispatch(Result.PreferredAudioQualityChanged(intent.quality))
                    preferenceManager.setPreferredAudioQuality(intent.quality)
                }

                is Intent.UpdateSpotifyCredentials -> {
                    dispatch(Result.SpotifyCredentialsUpdated(intent.credentials))
                    preferenceManager.setSpotifyCredentials(intent.credentials)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.AnalyticsToggled -> copy(isAnalyticsEnabled = result.isEnabled)
                is Result.DownloadPathSet -> copy(downloadPath = result.path)
                is Result.PreferredAudioQualityChanged -> copy(preferredQuality = result.quality)
                is Result.SpotifyCredentialsUpdated -> copy(spotifyCredentials = result.spotifyCredentials)
            }
    }
}
