package com.shabinder.common.models

import co.touchlab.stately.freeze
import kotlin.jvm.JvmStatic

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
    fun setDownloadDirectoryAction(callBack: (String) -> Unit)

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

    companion object {
        /*
        * Holder to call platform actions from anywhere
        * */
        @JvmStatic
        var instance: Actions
            get() = methodsAtomicRef.value
            set(value) {
                methodsAtomicRef.value = value
            }

        private val methodsAtomicRef = NativeAtomicReference(stubActions().freeze())
    }
}

private fun stubActions(): Actions = object : Actions {
    override val platformActions = StubPlatformActions
    override fun showPopUpMessage(string: String, long: Boolean) {}
    override fun setDownloadDirectoryAction(callBack: (String) -> Unit) {}
    override fun queryActiveTracks() {}
    override fun giveDonation() {}
    override fun shareApp() {}
    override fun copyToClipboard(text: String) {}

    override fun openPlatform(packageID: String, platformLink: String) {}
    override fun writeMp3Tags(trackDetails: TrackDetails) {}

    override val isInternetAvailable: Boolean = true
}
