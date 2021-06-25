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
import com.shabinder.common.di.Dir
import com.shabinder.common.di.Picture
import com.shabinder.common.di.preference.PreferenceManager
import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.Consumer
import com.shabinder.common.preference.integration.SpotiFlyerPreferenceImpl

interface SpotiFlyerPreference {

    val model: Value<State>

    val analytics: Analytics

    fun toggleAnalytics(enabled: Boolean)

    fun setDownloadDirectory(newBasePath: String)

    suspend fun loadImage(url: String): Picture

    interface Dependencies {
        val prefOutput: Consumer<Output>
        val storeFactory: StoreFactory
        val dir: Dir
        val preferenceManager: PreferenceManager
        val preferenceAnalytics: Analytics
    }

    interface Analytics

    sealed class Output {
        object Finished : Output()
    }

    data class State(
        val preferredQuality: AudioQuality = AudioQuality.KBPS320,
        val isAnalyticsEnabled: Boolean = false
    )
}

@Suppress("FunctionName") // Factory function
fun SpotiFlyerPreference(componentContext: ComponentContext, dependencies: SpotiFlyerPreference.Dependencies): SpotiFlyerPreference =
    SpotiFlyerPreferenceImpl(componentContext, dependencies)
