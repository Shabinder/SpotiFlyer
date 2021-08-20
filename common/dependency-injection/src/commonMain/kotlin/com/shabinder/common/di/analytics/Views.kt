package com.shabinder.common.di.analytics

import com.shabinder.common.di.analytics.AnalyticsManager.Companion.AnalyticsAction

sealed class AnalyticsView(private val viewName: String, private val extras: Map<String, Any> = emptyMap()) : AnalyticsAction() {
    override fun track(analyticsManager: AnalyticsManager) = analyticsManager.sendView(viewName,extras)

    object HomeScreen: AnalyticsView("home_screen")
    object ListScreen: AnalyticsView("list_screen")
}