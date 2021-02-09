package com.shabinder.common.list

import androidx.compose.ui.graphics.ImageBitmap
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.shabinder.common.di.Dir
import com.shabinder.common.di.FetchPlatformQueryResult
import com.shabinder.common.list.integration.SpotiFlyerListImpl
import com.shabinder.common.models.spotify.Source
import com.shabinder.common.utils.Consumer
import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.common.models.TrackDetails
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
    fun onDownloadClicked(wholeTrackList:List<TrackDetails>, trackIndex:Int)

    /*
    * To Pop and return back to Main Screen
    * */
    fun onBackPressed()

    /*
    * Load Image from cache/Internet and cache it
    * */
    suspend fun loadImage(url:String): ImageBitmap?

    interface Dependencies {
        val storeFactory: StoreFactory
        val fetchQuery: FetchPlatformQueryResult
        val dir: Dir
        val link: String
        val listOutput: Consumer<Output>
    }
    sealed class Output {
        object Finished : Output()
    }
    data class State(
        val queryResult: PlatformQueryResult? = PlatformQueryResult(
            "","",
            "Loading","", emptyList(),
            Source.Spotify),
        val link:String = ""
    )
}

@Suppress("FunctionName") // Factory function
fun SpotiFlyerList(componentContext: ComponentContext, dependencies: SpotiFlyerList.Dependencies): SpotiFlyerList =
    SpotiFlyerListImpl(componentContext, dependencies)
