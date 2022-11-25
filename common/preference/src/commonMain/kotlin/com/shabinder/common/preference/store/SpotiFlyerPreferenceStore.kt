/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.common.preference.store

import com.arkivanov.mvikotlin.core.store.Store
import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.spotify.SpotifyCredentials
import com.shabinder.common.preference.SpotiFlyerPreference

internal interface SpotiFlyerPreferenceStore : Store<SpotiFlyerPreferenceStore.Intent, SpotiFlyerPreference.State, Nothing> {
    sealed class Intent {
        data class OpenPlatform(val platformID: String, val platformLink: String) : Intent()
        data class ToggleAnalytics(val enabled: Boolean) : Intent()
        data class SetDownloadDirectory(val path: String) : Intent()
        data class SetPreferredAudioQuality(val quality: AudioQuality) : Intent()
        data class UpdateSpotifyCredentials(val credentials: SpotifyCredentials) : Intent()
        object GiveDonation : Intent()
        object ShareApp : Intent()
    }
}
