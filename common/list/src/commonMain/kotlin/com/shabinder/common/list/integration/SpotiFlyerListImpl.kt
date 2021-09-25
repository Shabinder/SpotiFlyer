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

package com.shabinder.common.list.integration

import co.touchlab.stately.ensureNeverFrozen
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnResume
import com.shabinder.common.caching.Cache
import com.shabinder.common.core_components.picture.Picture
import com.shabinder.common.core_components.utils.asValue
import com.shabinder.common.list.SpotiFlyerList
import com.shabinder.common.list.SpotiFlyerList.Dependencies
import com.shabinder.common.list.SpotiFlyerList.State
import com.shabinder.common.list.store.SpotiFlyerListStore.Intent
import com.shabinder.common.list.store.SpotiFlyerListStoreProvider
import com.shabinder.common.list.store.getStore
import com.shabinder.common.models.TrackDetails

internal class SpotiFlyerListImpl(
    componentContext: ComponentContext,
    dependencies: Dependencies
) : SpotiFlyerList, ComponentContext by componentContext, Dependencies by dependencies {

    init {
        instanceKeeper.ensureNeverFrozen()
        lifecycle.doOnResume {
            onRefreshTracksStatuses()
        }
    }

    private val store =
        instanceKeeper.getStore {
            SpotiFlyerListStoreProvider(dependencies).provide()
        }

    private val cache = Cache.Builder
        .newBuilder()
        .maximumCacheSize(30)
        .build<String, Picture>()

    override val model: Value<State> = store.asValue()

    override fun onDownloadAllClicked(trackList: List<TrackDetails>) {
        store.accept(Intent.StartDownloadAll(trackList))
    }

    override fun onDownloadClicked(track: TrackDetails) {
        store.accept(Intent.StartDownload(track))
    }

    override fun onBackPressed() {
        listOutput.callback(SpotiFlyerList.Output.Finished)
    }

    override fun onRefreshTracksStatuses() {
        store.accept(Intent.RefreshTracksStatuses)
    }

    override fun dismissDonationDialogSetOffset() {
        preferenceManager.setDonationOffset(offset = 10)
    }

    override suspend fun loadImage(url: String, isCover: Boolean): Picture {
        return cache.get(url) {
            if (isCover) fileManager.loadImage(url, 350, 350)
            else fileManager.loadImage(url, 150, 150)
        }
    }
}
