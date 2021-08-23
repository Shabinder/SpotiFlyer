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
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.shabinder.common.core_components.analytics.AnalyticsManager
import com.shabinder.common.core_components.file_manager.FileManager
import com.shabinder.common.core_components.picture.Picture
import com.shabinder.common.core_components.preference_manager.PreferenceManager
import com.shabinder.common.main.integration.SpotiFlyerMainImpl
import com.shabinder.common.models.Consumer
import com.shabinder.common.models.DownloadRecord
import com.shabinder.database.Database

interface SpotiFlyerMain {

    val model: Value<State>

    val analytics: Analytics

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
    * change TabBar Selected Category
    * */
    fun toggleAnalytics(enabled: Boolean)

    /*
    * Load Image from cache/Internet and cache it
    * */
    suspend fun loadImage(url: String): Picture

    fun dismissDonationDialogOffset()

    interface Dependencies {
        val mainOutput: Consumer<Output>
        val storeFactory: StoreFactory
        val database: Database?
        val fileManager: FileManager
        val preferenceManager: PreferenceManager
        val analyticsManager: AnalyticsManager
        val mainAnalytics: Analytics
    }

    interface Analytics {
        fun donationDialogVisit()
    }

    sealed class Output {
        data class Search(val link: String) : Output()
    }

    data class State(
        val records: List<DownloadRecord> = emptyList(),
        val link: String = "",
        val selectedCategory: HomeCategory = HomeCategory.About,
        val isAnalyticsEnabled: Boolean = false
    )

    enum class HomeCategory {
        About, History
    }
}

@Suppress("FunctionName") // Factory function
fun SpotiFlyerMain(componentContext: ComponentContext, dependencies: SpotiFlyerMain.Dependencies): SpotiFlyerMain =
    SpotiFlyerMainImpl(componentContext, dependencies)
