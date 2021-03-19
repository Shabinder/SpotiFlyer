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

package com.shabinder.common.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.shabinder.common.di.Dir
import com.shabinder.common.di.Picture
import com.shabinder.common.main.integration.SpotiFlyerMainImpl
import com.shabinder.common.models.Consumer
import com.shabinder.common.models.DownloadRecord
import com.shabinder.database.Database
import kotlinx.coroutines.flow.Flow

interface SpotiFlyerMain {

    val models: Flow<State>

    /*
    * We Intend to Move to List Screen
    * Note: Implementation in Root
    * */
    fun onLinkSearch(link: String)

    /*
    * Update TextBox's Text
    * */
    fun onInputLinkChanged(link: String)

    /*
    * change TabBar Selected Category
    * */
    fun selectCategory(category: HomeCategory)

    /*
    * Load Image from cache/Internet and cache it
    * */
    suspend fun loadImage(url: String): Picture

    interface Dependencies {
        val mainOutput: Consumer<Output>
        val storeFactory: StoreFactory
        val database: Database?
        val dir: Dir
        val showPopUpMessage: (String) -> Unit
    }

    sealed class Output {
        data class Search(val link: String) : Output()
    }

    data class State(
        val records: List<DownloadRecord> = emptyList(),
        val link: String = "",
        val selectedCategory: HomeCategory = HomeCategory.About
    )
    enum class HomeCategory {
        About, History
    }
}

@Suppress("FunctionName") // Factory function
fun SpotiFlyerMain(componentContext: ComponentContext, dependencies: SpotiFlyerMain.Dependencies): SpotiFlyerMain =
    SpotiFlyerMainImpl(componentContext, dependencies)
