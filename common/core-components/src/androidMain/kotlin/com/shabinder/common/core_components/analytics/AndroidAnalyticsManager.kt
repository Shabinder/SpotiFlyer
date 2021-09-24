package com.shabinder.common.core_components.analytics

import android.app.Activity
import android.app.Application
import ly.count.android.sdk.Countly
import ly.count.android.sdk.CountlyConfig
import ly.count.android.sdk.DeviceId
import org.koin.dsl.bind
import org.koin.dsl.module

internal class AndroidAnalyticsManager(private val mainActivity: Activity) : AnalyticsManager {

    companion object {
        private var isInitialised = false
    }

    init {
        // Don't Init If Instantiated on Diff Activities
        if (!isInitialised) {
            isInitialised = true
            init()
        }
    }

    override fun init() {
        Countly.sharedInstance().init(
            CountlyConfig(
                mainActivity.applicationContext as Application,
                COUNTLY_CONFIG.APP_KEY,
                COUNTLY_CONFIG.SERVER_URL
            ).apply {
                setIdMode(DeviceId.Type.OPEN_UDID)
                setViewTracking(true)
                enableCrashReporting()
                setLoggingEnabled(false)
                setRecordAllThreadsWithCrash()
                setRequiresConsent(true)
                setShouldIgnoreAppCrawlers(true)
                setEventQueueSizeToSend(5)
            }
        )
    }

    override fun onStart() {
        Countly.sharedInstance().onStart(mainActivity)
    }

    override fun onStop() {
        Countly.sharedInstance().onStop()
    }

    override fun giveConsent() {
        Countly.sharedInstance().consent().giveConsentAll()
    }

    override fun isTracking(): Boolean =
        Countly.sharedInstance().consent().getConsent(Countly.CountlyFeatureNames.events)

    override fun revokeConsent() {
        Countly.sharedInstance().consent().removeConsentAll()
    }

    override fun sendView(name: String, extras: MutableMap<String, Any>) {
        Countly.sharedInstance().views().recordView(name, extras)
    }

    override fun sendEvent(eventName: String, extras: MutableMap<String, Any>) {
        Countly.sharedInstance().events().recordEvent(eventName, extras)
    }

    override fun sendCrashReport(error: Throwable, extras: MutableMap<String, Any>) {
        Countly.sharedInstance().crashes().recordUnhandledException(error, extras)
    }
}

internal actual fun analyticsModule() = module {
    factory { (mainActivity: Activity) ->
        AndroidAnalyticsManager(mainActivity)
    } bind AnalyticsManager::class
}