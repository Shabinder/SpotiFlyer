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

package com.shabinder.common.preference.integration

import co.touchlab.stately.ensureNeverFrozen
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.shabinder.common.caching.Cache
import com.shabinder.common.core_components.picture.Picture
import com.shabinder.common.core_components.utils.asValue
import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.spotify.SpotifyCredentials
import com.shabinder.common.preference.SpotiFlyerPreference
import com.shabinder.common.preference.SpotiFlyerPreference.Dependencies
import com.shabinder.common.preference.SpotiFlyerPreference.State
import com.shabinder.common.preference.store.SpotiFlyerPreferenceStore.Intent
import com.shabinder.common.preference.store.SpotiFlyerPreferenceStoreProvider
import com.shabinder.common.preference.store.getStore

internal class SpotiFlyerPreferenceImpl(
    componentContext: ComponentContext,
    dependencies: Dependencies
) : SpotiFlyerPreference, ComponentContext by componentContext, Dependencies by dependencies {

    init {
        instanceKeeper.ensureNeverFrozen()
    }

    private val store =
        instanceKeeper.getStore {
            SpotiFlyerPreferenceStoreProvider(dependencies).provide()
        }

    private val cache = Cache.Builder
        .newBuilder()
        .maximumCacheSize(10)
        .build<String, Picture>()

    override val model: Value<State> = store.asValue()

    override val analytics = preferenceAnalytics

    override fun toggleAnalytics(enabled: Boolean) {
        store.accept(Intent.ToggleAnalytics(enabled))
    }

    override fun selectNewDownloadDirectory() {
        actions.setDownloadDirectoryAction {
            store.accept(Intent.SetDownloadDirectory(it))
        }
    }

    override fun setPreferredQuality(quality: AudioQuality) {
        store.accept(Intent.SetPreferredAudioQuality(quality))
    }

    override fun updateSpotifyCredentials(credentials: SpotifyCredentials) {
        store.accept(Intent.UpdateSpotifyCredentials(credentials))
    }

    override suspend fun loadImage(url: String): Picture {
        return cache.get(url) {
            fileManager.loadImage(url, 150, 150)
        }
    }
}
