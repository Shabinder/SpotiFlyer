package com.shabinder.common.core_components.analytics

import org.koin.dsl.bind
import org.koin.dsl.module

// TODO("Not yet implemented")
private val webAnalytics =
    object : AnalyticsManager {
        override fun init() {}

        override fun onStart() {}

        override fun onStop() {}

        override fun giveConsent() {}

        override fun isTracking(): Boolean = false

        override fun revokeConsent() {}

        override fun sendView(name: String, extras: MutableMap<String, Any>) {}

        override fun sendEvent(eventName: String, extras: MutableMap<String, Any>) {}

        override fun sendCrashReport(error: Throwable, extras: MutableMap<String, Any>) {}
    }

actual fun analyticsModule() = module {
    single { webAnalytics } bind AnalyticsManager::class
}