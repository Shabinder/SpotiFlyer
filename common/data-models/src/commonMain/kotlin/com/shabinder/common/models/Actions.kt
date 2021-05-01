package com.shabinder.common.models

import co.touchlab.stately.freeze
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/*
* Holder to call platform actions from anywhere
* */
val methods: NativeAtomicReference<Actions> = NativeAtomicReference(stubActions().freeze())

/*
* Interface Having All Platform Dependent Functions
* */
interface Actions {

    // Platform Specific Actions
    val platformActions: PlatformActions

    // Show Toast
    fun showPopUpMessage(string: String, long: Boolean = false)

    // Change Download Directory
    fun setDownloadDirectoryAction()

    /*
    * Query Downloading Tracks
    * ex- Get Tracks from android service etc
    * */
    fun queryActiveTracks()

    // Donate Money
    fun giveDonation()

    // Share SpotiFlyer App
    fun shareApp()

    // Open / Redirect to another Platform
    fun openPlatform(packageID: String, platformLink: String)

    // IO-Dispatcher
    val dispatcherIO: CoroutineDispatcher

    // Internet Connectivity Check
    val isInternetAvailable: Boolean

    // Current Platform Info
    val currentPlatform: AllPlatforms
}


private fun stubActions() = object :Actions{
    override val platformActions = object: PlatformActions{}
    override fun showPopUpMessage(string: String, long: Boolean) {}
    override fun setDownloadDirectoryAction() {}
    override fun queryActiveTracks() {}
    override fun giveDonation() {}
    override fun shareApp() {}
    override fun openPlatform(packageID: String, platformLink: String) {}
    override val dispatcherIO: CoroutineDispatcher = Dispatchers.Default
    override val isInternetAvailable: Boolean = true
    override val currentPlatform: AllPlatforms = AllPlatforms.Jvm
}