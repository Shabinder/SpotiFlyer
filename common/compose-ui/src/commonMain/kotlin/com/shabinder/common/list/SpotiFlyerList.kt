package com.shabinder.common.list

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.shabinder.common.FetchPlatformQueryResult
import com.shabinder.common.PlatformQueryResult
import com.shabinder.common.TrackDetails
import com.shabinder.common.list.integration.SpotiFlyerListImpl
import com.shabinder.common.utils.Consumer
import com.shabinder.database.Database
import kotlinx.coroutines.flow.Flow

interface SpotiFlyerList {

    val models: Flow<State>

    /*
    * Download All Tracks(after filtering already Downloaded)
    * */
    fun onDownloadAllClicked(trackList:List<TrackDetails>)
    /*
    * Download All Tracks(after filtering already Downloaded)
    * */
    fun onDownloadClicked(wholeTrackList:List<TrackDetails>,trackIndex:Int)

    /*
    * To Pop and return back to Main Screen
    * */
    fun onBackPressed()

    interface Dependencies {
        val storeFactory: StoreFactory
        val fetchQuery: FetchPlatformQueryResult
        val link: String
        fun listOutput(finished: Output.Finished): Consumer<Output>
    }
    sealed class Output {
        object Finished : Output()
    }
    data class State(
        val queryResult:PlatformQueryResult? = null,
        val link:String = ""
    )
}

@Suppress("FunctionName") // Factory function
fun SpotiFlyerList(componentContext: ComponentContext, dependencies: SpotiFlyerList.Dependencies): SpotiFlyerList =
    SpotiFlyerListImpl(componentContext, dependencies)
