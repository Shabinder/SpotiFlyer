package com.shabinder.common.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.RouterState
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.shabinder.common.Dir
import com.shabinder.common.FetchPlatformQueryResult
import com.shabinder.common.list.SpotiFlyerList
import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.root.SpotiFlyerRoot.Dependencies
import com.shabinder.database.Database
import com.shabinder.common.root.integration.SpotiFlyerRootImpl

interface SpotiFlyerRoot {

    val routerState: Value<RouterState<*, Child>>

    sealed class Child {
        data class Main(val component: SpotiFlyerMain) : Child()
        data class List(val component: SpotiFlyerList) : Child()
    }

    interface Dependencies {
        val storeFactory: StoreFactory
        val database: Database
        val fetchPlatformQueryResult: FetchPlatformQueryResult
        val directories: Dir
    }
}

@Suppress("FunctionName") // Factory function
fun SpotiFlyerRoot(componentContext: ComponentContext, dependencies: Dependencies): SpotiFlyerRoot =
    SpotiFlyerRootImpl(componentContext, dependencies)
