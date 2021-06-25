package com.shabinder.common.di.preference

import com.russhwolf.settings.Settings

class PreferenceManager(settings: Settings): Settings by settings {

    companion object {
        const val DirKey = "downloadDir"
        const val AnalyticsKey = "analytics"
        const val FirstLaunch = "firstLaunch"
        const val DonationInterval = "donationInterval"
    }

    /* ANALYTICS */
    val isAnalyticsEnabled get() = getBooleanOrNull(AnalyticsKey) ?: false
    fun toggleAnalytics(enabled: Boolean) = putBoolean(AnalyticsKey, enabled)


    /* DOWNLOAD DIRECTORY */
    val downloadDir get() = getStringOrNull(DirKey)
    fun setDownloadDirectory(newBasePath: String) = putString(DirKey, newBasePath)


    /* OFFSET FOR WHEN TO ASK FOR SUPPORT */
    val getDonationOffset: Int get() = (getIntOrNull(DonationInterval) ?: 3).also {
        // Min. Donation Asking Interval is `3`
        if (it < 3) setDonationOffset(3) else setDonationOffset(it - 1)
    }
    fun setDonationOffset(offset: Int = 5) = putInt(DonationInterval, offset)


    /* TO CHECK IF THIS IS APP's FIRST LAUNCH */
    val isFirstLaunch get() = getBooleanOrNull(FirstLaunch) ?: true
    fun firstLaunchDone() = putBoolean(FirstLaunch, false)
}