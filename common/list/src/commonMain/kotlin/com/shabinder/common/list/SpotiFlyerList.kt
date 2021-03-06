package com.shabinder.common.list

import androidx.compose.ui.graphics.ImageBitmap
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.shabinder.common.di.Dir
import com.shabinder.common.di.FetchPlatformQueryResult
import com.shabinder.common.di.Picture
import com.shabinder.common.list.integration.SpotiFlyerListImpl
import com.shabinder.common.models.Consumer
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.common.models.TrackDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

interface SpotiFlyerList {

    val models: Flow<State>

    /*
    * Download All Tracks(after filtering already Downloaded)
    * */
    fun onDownloadAllClicked(trackList:List<TrackDetails>)

    /*
    * Download All Tracks(after filtering already Downloaded)
    * */
    fun onDownloadClicked(track:TrackDetails)

    /*
    * To Pop and return back to Main Screen
    * */
    fun onBackPressed()

    /*
    * Load Image from cache/Internet and cache it
    * */
    suspend fun loadImage(url:String): Picture

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
    }
    sealed class Output {
        object Finished : Output()
    }
    data class State(
        val queryResult: PlatformQueryResult? = null,
        val link:String = "",
        val trackList:List<TrackDetails> = emptyList()
    )
}

@Suppress("FunctionName") // Factory function
fun SpotiFlyerList(componentContext: ComponentContext, dependencies: SpotiFlyerList.Dependencies): SpotiFlyerList =
    SpotiFlyerListImpl(componentContext, dependencies)
