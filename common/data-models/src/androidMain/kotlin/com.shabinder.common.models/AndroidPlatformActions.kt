package com.shabinder.common.models

import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope

actual interface PlatformActions {

    companion object {
        const val SharedPreferencesKey = "configurations"
    }

    val imageCacheDir: String

    val sharedPreferences: SharedPreferences?

    fun addToLibrary(path: String)

    fun sendTracksToService(array: List<TrackDetails>)
}

internal actual val StubPlatformActions = object : PlatformActions {
    override val imageCacheDir = ""

    override val sharedPreferences: SharedPreferences? = null

    override fun addToLibrary(path: String) {}

    override fun sendTracksToService(array: List<TrackDetails>) {}
}

actual fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T = kotlinx.coroutines.runBlocking { block() }