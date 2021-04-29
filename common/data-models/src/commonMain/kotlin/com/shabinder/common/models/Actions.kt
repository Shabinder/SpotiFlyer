package com.shabinder.common.models

import kotlinx.coroutines.CoroutineDispatcher

/*
* Holder to call platform actions from anywhere
* */
lateinit var methods: Actions

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