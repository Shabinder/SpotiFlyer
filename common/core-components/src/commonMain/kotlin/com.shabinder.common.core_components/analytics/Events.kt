package com.shabinder.common.core_components.analytics

sealed class AnalyticsEvent(private val eventName: String, private val extras: MutableMap<String, Any> = mutableMapOf()): AnalyticsManager.Companion.AnalyticsAction() {

    override fun track(analyticsManager: AnalyticsManager) = analyticsManager.sendEvent(eventName,extras)

    object AppLaunch: AnalyticsEvent("app_launch")
    object DonationDialogOpen: AnalyticsEvent("donation_dialog_open")
}