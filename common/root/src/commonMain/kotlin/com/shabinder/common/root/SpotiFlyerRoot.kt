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

package com.shabinder.common.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.RouterState
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.shabinder.common.di.Dir
import com.shabinder.common.di.FetchPlatformQueryResult
import com.shabinder.common.list.SpotiFlyerList
import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.root.SpotiFlyerRoot.Dependencies
import com.shabinder.common.root.callbacks.SpotiFlyerRootCallBacks
import com.shabinder.common.root.integration.SpotiFlyerRootImpl
import com.shabinder.database.Database
import kotlinx.coroutines.flow.MutableSharedFlow

interface SpotiFlyerRoot {

    val routerState: Value<RouterState<*, Child>>

    val callBacks: SpotiFlyerRootCallBacks

    sealed class Child {
        data class Main(val component: SpotiFlyerMain) : Child()
        data class List(val component: SpotiFlyerList) : Child()
    }

    interface Dependencies {
        val storeFactory: StoreFactory
        val database: Database?
        val fetchPlatformQueryResult: FetchPlatformQueryResult
        val directories: Dir
        val showPopUpMessage: (String) -> Unit
        val downloadProgressReport: MutableSharedFlow<HashMap<String, DownloadStatus>>
        val setDownloadDirectoryAction:()->Unit
    }
}

@Suppress("FunctionName") // Factory function
fun SpotiFlyerRoot(componentContext: ComponentContext, dependencies: Dependencies): SpotiFlyerRoot =
    SpotiFlyerRootImpl(componentContext, dependencies)
