package com.shabinder.common.core_components.analytics

import com.shabinder.common.core_components.analytics.AnalyticsManager.Companion.AnalyticsAction

sealed class AnalyticsView(private val viewName: String, private val extras: MutableMap<String, Any> = mutableMapOf()) : AnalyticsAction() {
    override fun track(analyticsManager: AnalyticsManager) = analyticsManager.sendView(viewName,extras)

    object HomeScreen: AnalyticsView("home_screen")
    object ListScreen: AnalyticsView("list_screen")
}