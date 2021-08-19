package com.shabinder.common.di.analytics

import com.shabinder.common.di.Dir
import ly.count.sdk.java.Config
import ly.count.sdk.java.Config.DeviceIdStrategy
import ly.count.sdk.java.Config.Feature
import ly.count.sdk.java.ConfigCore.LoggingLevel
import ly.count.sdk.java.Countly
import org.koin.dsl.bind
import org.koin.dsl.module
import java.io.File

internal class DesktopAnalyticsManager(
    private val dir: Dir
) : AnalyticsManager {

    init {
        init()
    }

    override fun init() {

        val config: Config = Config(COUNTLY_CONFIG.SERVER_URL, COUNTLY_CONFIG.APP_KEY).apply {
            eventsBufferSize = 2
            loggingLevel = LoggingLevel.DEBUG
            setDeviceIdStrategy(DeviceIdStrategy.UUID)
            enableFeatures(*featuresSet)
            setRequiresConsent(true)
        }

        Countly.init(File(dir.defaultDir()), config)

        Countly.session().begin();
    }

    override fun giveConsent() {
        Countly.onConsent(*featuresSet)
    }

    override fun isTracking(): Boolean = Countly.isTracking(Feature.Events)

    override fun revokeConsent() {
        Countly.onConsentRemoval(*featuresSet)
    }

    override fun sendView(name: String, extras: Map<String, Any>) {
        Countly.api().view(name)
    }

    override fun sendEvent(eventName: String, extras: Map<String, Any>) {
        Countly.api().event(eventName)
            .setSegmentation(extras.filterValues { it is String } as? Map<String, String> ?: emptyMap()).record()
    }

    override fun sendCrashReport(error: Throwable, extras: Map<String, Any>) {
        Countly.api().addCrashReport(
            error,
            extras.getOrDefault("fatal", true) as Boolean,
            error.javaClass.simpleName,
            extras.filterValues { it is String } as? Map<String, String> ?: emptyMap()
        )
    }

    companion object {
        val featuresSet = arrayOf(
            Feature.Events,
            Feature.Sessions,
            Feature.CrashReporting,
            Feature.Views,
            Feature.UserProfiles,
            Feature.Location,
        )
    }

    override fun onStart() {}

    override fun onStop() {}
}

actual fun analyticsModule() = module {
    single { DesktopAnalyticsManager(get()) } bind AnalyticsManager::class
}