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

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.shabinder.common.di.Picture
import com.shabinder.common.di.isInternetAvailable
import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.main.SpotiFlyerMain.*
import com.shabinder.common.main.store.SpotiFlyerMainStore.Intent
import com.shabinder.common.main.store.SpotiFlyerMainStoreProvider
import com.shabinder.common.main.store.getStore
import kotlinx.coroutines.flow.Flow

internal class SpotiFlyerMainImpl(
    componentContext: ComponentContext,
    dependencies: Dependencies
): SpotiFlyerMain,ComponentContext by componentContext, Dependencies by dependencies {

    private val store =
        instanceKeeper.getStore {
            SpotiFlyerMainStoreProvider(
                storeFactory = storeFactory,
                database = database,
                showPopUpMessage = showPopUpMessage
            ).provide()
        }

    override val models: Flow<State> = store.states

    override fun onLinkSearch(link: String) {
        if(isInternetAvailable) mainOutput.callback(Output.Search(link = link))
        else showPopUpMessage("Check Network Connection Please")
    }

    override fun onInputLinkChanged(link: String) {
        store.accept(Intent.SetLink(link))
    }

    override fun selectCategory(category: HomeCategory) {
        store.accept(Intent.SelectCategory(category))
    }

    override suspend fun loadImage(url: String): Picture = dir.loadImage(url)
}