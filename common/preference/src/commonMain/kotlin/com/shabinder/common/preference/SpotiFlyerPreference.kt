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

package com.shabinder.common.preference

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.shabinder.common.core_components.analytics.AnalyticsManager
import com.shabinder.common.core_components.file_manager.FileManager
import com.shabinder.common.core_components.picture.Picture
import com.shabinder.common.core_components.preference_manager.PreferenceManager
import com.shabinder.common.models.Actions
import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.Consumer
import com.shabinder.common.models.spotify.SpotifyCredentials
import com.shabinder.common.preference.integration.SpotiFlyerPreferenceImpl

interface SpotiFlyerPreference {

    val model: Value<State>

    val analytics: Analytics

    fun toggleAnalytics(enabled: Boolean)

    fun selectNewDownloadDirectory()

    fun setPreferredQuality(quality: AudioQuality)

    fun updateSpotifyCredentials(credentials: SpotifyCredentials)

    suspend fun loadImage(url: String): Picture

    interface Dependencies {
        val prefOutput: Consumer<Output>
        val storeFactory: StoreFactory
        val fileManager: FileManager
        val preferenceManager: PreferenceManager
        val analyticsManager: AnalyticsManager
        val actions: Actions
        val preferenceAnalytics: Analytics
    }

    interface Analytics

    sealed class Output {
        object Finished : Output()
    }

    data class State(
        val preferredQuality: AudioQuality = AudioQuality.KBPS320,
        val downloadPath: String = "",
        val isAnalyticsEnabled: Boolean = false,
        val spotifyCredentials: SpotifyCredentials = SpotifyCredentials()
    )
}

@Suppress("FunctionName") // Factory function
fun SpotiFlyerPreference(
    componentContext: ComponentContext,
    dependencies: SpotiFlyerPreference.Dependencies
): SpotiFlyerPreference =
    SpotiFlyerPreferenceImpl(componentContext, dependencies)
