package com.shabinder.common.models

import co.touchlab.stately.freeze

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

    // Platform Specific Implementation Preferred
    val isInternetAvailable: Boolean

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

    // Copy to Clipboard
    fun copyToClipboard(text: String)

    // Open / Redirect to another Platform
    fun openPlatform(packageID: String, platformLink: String)
    fun writeMp3Tags(trackDetails: TrackDetails)
}

private fun stubActions(): Actions = object : Actions {
    override val platformActions = StubPlatformActions
    override fun showPopUpMessage(string: String, long: Boolean) {}
    override fun setDownloadDirectoryAction() {}
    override fun queryActiveTracks() {}
    override fun giveDonation() {}
    override fun shareApp() {}
    override fun copyToClipboard(text: String) {}

    override fun openPlatform(packageID: String, platformLink: String) {}
    override fun writeMp3Tags(trackDetails: TrackDetails) {}

    override val isInternetAvailable: Boolean = true
}
