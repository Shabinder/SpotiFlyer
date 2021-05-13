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

package com.shabinder.common.list

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.shabinder.common.di.Dir
import com.shabinder.common.di.FetchPlatformQueryResult
import com.shabinder.common.di.Picture
import com.shabinder.common.list.integration.SpotiFlyerListImpl
import com.shabinder.common.models.Consumer
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.common.models.TrackDetails
import kotlinx.coroutines.flow.MutableSharedFlow

interface SpotiFlyerList {

    val models: Value<State>

    /*
    * Download All Tracks(after filtering already Downloaded)
    * */
    fun onDownloadAllClicked(trackList: List<TrackDetails>)

    /*
    * Download All Tracks(after filtering already Downloaded)
    * */
    fun onDownloadClicked(track: TrackDetails)

    /*
    * To Pop and return back to Main Screen
    * */
    fun onBackPressed()

    /*
    * Load Image from cache/Internet and cache it
    * */
    suspend fun loadImage(url: String): Picture

    /*
    * Sync Tracks Statuses
    * */
    fun onRefreshTracksStatuses()

    interface Dependencies {
        val storeFactory: StoreFactory
        val fetchQuery: FetchPlatformQueryResult
        val dir: Dir
        val link: String
        val listOutput: Consumer<Output>
        val downloadProgressFlow: MutableSharedFlow<HashMap<String, DownloadStatus>>
        val listAnalytics: Analytics
    }

    interface Analytics {

    }

    sealed class Output {
        object Finished : Output()
    }

    data class State(
        val queryResult: PlatformQueryResult? = null,
        val link: String = "",
        val trackList: List<TrackDetails> = emptyList(),
        val errorOccurred: Exception? = null
    )
}

@Suppress("FunctionName") // Factory function
fun SpotiFlyerList(componentContext: ComponentContext, dependencies: SpotiFlyerList.Dependencies): SpotiFlyerList =
    SpotiFlyerListImpl(componentContext, dependencies)
