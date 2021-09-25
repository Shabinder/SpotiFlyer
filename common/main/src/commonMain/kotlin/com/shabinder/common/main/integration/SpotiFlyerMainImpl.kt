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

package com.shabinder.common.main.integration

import co.touchlab.stately.ensureNeverFrozen
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnResume
import com.shabinder.common.caching.Cache
import com.shabinder.common.core_components.picture.Picture
import com.shabinder.common.core_components.utils.asValue
import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.main.SpotiFlyerMain.*
import com.shabinder.common.main.store.SpotiFlyerMainStore.Intent
import com.shabinder.common.main.store.SpotiFlyerMainStoreProvider
import com.shabinder.common.main.store.getStore
import com.shabinder.common.models.Actions

internal class SpotiFlyerMainImpl(
    componentContext: ComponentContext,
    dependencies: Dependencies
) : SpotiFlyerMain, ComponentContext by componentContext, Dependencies by dependencies {

    init {
        instanceKeeper.ensureNeverFrozen()
        lifecycle.doOnResume {
            store.accept(Intent.ToggleAnalytics(preferenceManager.isAnalyticsEnabled))
        }
    }

    private val store =
        instanceKeeper.getStore {
            SpotiFlyerMainStoreProvider(dependencies).provide()
        }

    private val cache = Cache.Builder
        .newBuilder()
        .maximumCacheSize(20)
        .build<String, Picture>()

    override val model: Value<State> = store.asValue()

    override val analytics = mainAnalytics

    override fun onLinkSearch(link: String) {
        if (Actions.instance.isInternetAvailable) mainOutput.callback(Output.Search(link = link))
        else Actions.instance.showPopUpMessage("Check Network Connection Please")
    }

    override fun onInputLinkChanged(link: String) {
        store.accept(Intent.SetLink(link))
    }

    override fun selectCategory(category: HomeCategory) {
        store.accept(Intent.SelectCategory(category))
    }

    override fun toggleAnalytics(enabled: Boolean) {
        store.accept(Intent.ToggleAnalytics(enabled))
    }

    override suspend fun loadImage(url: String): Picture {
        return cache.get(url) {
            fileManager.loadImage(url, 150, 150)
        }
    }

    override fun dismissDonationDialogOffset() {
        preferenceManager.setDonationOffset()
    }
}
