package com.shabinder.common.core_components.preference_manager

import co.touchlab.stately.annotation.Throws
import com.russhwolf.settings.Settings
import com.shabinder.common.core_components.analytics.AnalyticsManager
import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.spotify.SpotifyCredentials
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.native.concurrent.ThreadLocal

class PreferenceManager(
    settings: Settings,
) : Settings by settings {

    companion object {
        const val DIR_KEY = "downloadDir"
        const val ANALYTICS_KEY = "analytics"
        const val FIRST_LAUNCH = "firstLaunch"
        const val DONATION_INTERVAL = "donationInterval"
        const val PREFERRED_AUDIO_QUALITY = "preferredAudioQuality"

        @Suppress("VARIABLE_IN_SINGLETON_WITHOUT_THREAD_LOCAL")
        lateinit var instance: PreferenceManager
            private set
    }

    init {
        instance = this
    }

    lateinit var analyticsManager: AnalyticsManager

    /* ANALYTICS */
     val isAnalyticsEnabled get() = getBooleanOrNull(ANALYTICS_KEY) ?: false
     fun toggleAnalytics(enabled: Boolean) {
         putBoolean(ANALYTICS_KEY, enabled)
         if (this::analyticsManager.isInitialized) {
             if (enabled) analyticsManager.giveConsent() else analyticsManager.revokeConsent()
         }
     }

    /* DOWNLOAD DIRECTORY */
    val downloadDir get() = getStringOrNull(DIR_KEY)
    fun setDownloadDirectory(newBasePath: String) = putString(DIR_KEY, newBasePath)

    /* Preferred Audio Quality */
    val audioQuality get() = AudioQuality.getQuality(getStringOrNull(PREFERRED_AUDIO_QUALITY) ?: "320")
    fun setPreferredAudioQuality(quality: AudioQuality) = putString(PREFERRED_AUDIO_QUALITY, quality.kbps)

    val spotifyCredentials: SpotifyCredentials get() = getStringOrNull("spotifyCredentials")?.let {
        Json.decodeFromString(it)
    } ?: SpotifyCredentials()
    fun setSpotifyCredentials(credentials: SpotifyCredentials) = putString("spotifyCredentials", Json.encodeToString(SpotifyCredentials.serializer(), credentials))

    /* OFFSET FOR WHEN TO ASK FOR SUPPORT */
    val getDonationOffset: Int get() = (getIntOrNull(DONATION_INTERVAL) ?: 3).also {
        // Min. Donation Asking Interval is `3`
        if (it < 3) setDonationOffset(3) else setDonationOffset(it - 1)
    }
    fun setDonationOffset(offset: Int = 5) = putInt(DONATION_INTERVAL, offset)

    /* TO CHECK IF THIS IS APP's FIRST LAUNCH */
    val isFirstLaunch get() = getBooleanOrNull(FIRST_LAUNCH) ?: true
    fun firstLaunchDone() = putBoolean(FIRST_LAUNCH, false)
}
