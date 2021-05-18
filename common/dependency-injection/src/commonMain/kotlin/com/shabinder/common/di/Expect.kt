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
import io.ktor.client.request.head
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.native.concurrent.SharedImmutable

expect suspend fun downloadTracks(
    list: List<TrackDetails>,
    fetcher: FetchPlatformQueryResult,
    dir: Dir
)

// IO-Dispatcher
@SharedImmutable
expect val dispatcherIO: CoroutineDispatcher

// Current Platform Info
@SharedImmutable
expect val currentPlatform: AllPlatforms

suspend fun isInternetAccessible(): Boolean {
    return withContext(dispatcherIO) {
        try {
            ktorHttpClient.head<String>("https://google.com")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

internal val dispatcherDefault = Dispatchers.Default
