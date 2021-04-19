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

package com.shabinder.common.di

import com.shabinder.common.models.AllPlatforms
import com.shabinder.common.models.TrackDetails
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

expect fun openPlatform(packageID: String, platformLink: String)

expect fun shareApp()

expect fun giveDonation()

expect val dispatcherIO: CoroutineDispatcher

expect val isInternetAvailable: Boolean

expect val currentPlatform: AllPlatforms

expect suspend fun downloadTracks(
    list: List<TrackDetails>,
    fetcher: FetchPlatformQueryResult,
    dir: Dir
)

expect fun queryActiveTracks()

/*
* Refactor This
* */
suspend fun isInternetAccessible(): Boolean {
    return withContext(dispatcherIO) {
        try {
            ktorHttpClient.head<String>("http://google.com")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
