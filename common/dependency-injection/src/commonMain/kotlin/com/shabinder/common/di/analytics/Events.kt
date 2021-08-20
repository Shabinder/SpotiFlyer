package com.shabinder.common.di.analytics

sealed class AnalyticsEvent(private val eventName: String, private val extras: Map<String, Any> = emptyMap()): AnalyticsManager.Companion.AnalyticsAction() {

    override fun track(analyticsManager: AnalyticsManager) = analyticsManager.sendEvent(eventName,extras)

    object AppLaunch: AnalyticsEvent("app_launch")
    object DonationDialogOpen: AnalyticsEvent("donation_dialog_open")
}