package com.shabinder.common.core_components.analytics

import android.app.Activity
import android.app.Application
import ly.count.android.sdk.Countly
import ly.count.android.sdk.CountlyConfig
import ly.count.android.sdk.DeviceId
import org.koin.dsl.bind
import org.koin.dsl.module

internal class AndroidAnalyticsManager(private val mainActivity: Activity) : AnalyticsManager {

    init {
        init()
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
                setLoggingEnabled(true)
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

    override fun isTracking(): Boolean = Countly.sharedInstance().consent().getConsent(Countly.CountlyFeatureNames.events)

    override fun revokeConsent() {
        Countly.sharedInstance().consent().removeConsentAll()
    }

    override fun sendView(name: String, extras: Map<String, Any>) {
        Countly.sharedInstance().views().recordView(name, extras)
    }

    override fun sendEvent(eventName: String, extras: Map<String, Any>) {
        Countly.sharedInstance().events().recordEvent(eventName, extras)
    }

    override fun sendCrashReport(error: Throwable, extras: Map<String, Any>) {
        Countly.sharedInstance().crashes().recordUnhandledException(error, extras)
    }
}

internal actual fun analyticsModule() = module {
    factory { (mainActivity: Activity) ->
        AndroidAnalyticsManager(mainActivity)
    } bind AnalyticsManager::class
}