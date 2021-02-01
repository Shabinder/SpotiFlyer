package com.shabinder.common.list

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.shabinder.common.PlatformQueryResult
import com.shabinder.common.TrackDetails
import com.shabinder.common.list.integration.SpotiFlyerListImpl
import com.shabinder.database.Database
import kotlinx.coroutines.flow.Flow

interface SpotiFlyerList {

    val models: Flow<State>

    /*
    * For Single Track Download -> list(that track)
    * For Download All -> Model.tracks
    * */
    fun onDownloadClicked(trackList:List<TrackDetails>)

    /*
    * To Pop and return back to Main Screen
    * */
    fun onBackPressed()

    interface Dependencies {
        val storeFactory: StoreFactory
        val database: Database
        val link: String
        fun listOutput(finished: Output.Finished)
    }
    sealed class Output {
        object Finished : Output()
    }
    data class State(
        val result:PlatformQueryResult? = null,
        val link:String = ""
    )
}

@Suppress("FunctionName") // Factory function
fun SpotiFlyerList(componentContext: ComponentContext, dependencies: SpotiFlyerList.Dependencies): SpotiFlyerList =
    SpotiFlyerListImpl(componentContext, dependencies)
