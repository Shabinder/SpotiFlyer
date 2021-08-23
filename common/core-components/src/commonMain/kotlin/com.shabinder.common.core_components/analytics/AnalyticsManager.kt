package com.shabinder.common.core_components.analytics

import org.koin.core.module.Module

interface AnalyticsManager {
    fun init()
    fun onStart()
    fun onStop()
    fun giveConsent()
    fun isTracking(): Boolean
    fun revokeConsent()
    fun sendView(name: String, extras: Map<String, Any> = emptyMap())
    fun sendEvent(eventName: String, extras: Map<String, Any> = emptyMap())
    fun track(event: AnalyticsAction) = event.track(this)
    fun sendCrashReport(error: Throwable, extras: Map<String, Any> = emptyMap())

    companion object {
        abstract class AnalyticsAction {
            abstract fun track(analyticsManager: AnalyticsManager)
        }
    }
}

@Suppress("ClassName", "SpellCheckingInspection")
object COUNTLY_CONFIG {
    const val APP_KEY = "27820f304468cc651ef47d787f0cb5fe11c577df"
    const val SERVER_URL = "https://counlty.shabinder.in"
}

internal expect fun analyticsModule(): Module