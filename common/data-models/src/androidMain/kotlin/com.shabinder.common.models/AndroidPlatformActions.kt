package com.shabinder.common.models

import android.content.SharedPreferences

actual interface PlatformActions {

    companion object {
        const val SharedPreferencesKey = "configurations"
    }

    val imageCacheDir: String

    val sharedPreferences: SharedPreferences

    fun addToLibrary(path: String)

    fun sendTracksToService(array: ArrayList<TrackDetails>)
}