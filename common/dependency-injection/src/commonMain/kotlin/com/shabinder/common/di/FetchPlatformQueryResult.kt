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

import com.shabinder.common.database.DownloadRecordDatabaseQueries
import com.shabinder.common.di.providers.GaanaProvider
import com.shabinder.common.di.providers.SaavnProvider
import com.shabinder.common.di.providers.SpotifyProvider
import com.shabinder.common.di.providers.YoutubeMp3
import com.shabinder.common.di.providers.YoutubeMusic
import com.shabinder.common.di.providers.YoutubeProvider
import com.shabinder.common.models.PlatformQueryResult
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FetchPlatformQueryResult(
    val gaanaProvider: GaanaProvider,
    val spotifyProvider: SpotifyProvider,
    val youtubeProvider: YoutubeProvider,
    val saavnProvider: SaavnProvider,
    val youtubeMusic: YoutubeMusic,
    val youtubeMp3: YoutubeMp3,
    val dir: Dir
) {
    private val db: DownloadRecordDatabaseQueries?
        get() = dir.db?.downloadRecordDatabaseQueries

    suspend fun query(link: String): PlatformQueryResult? {
        val result = when {
            // SPOTIFY
            link.contains("spotify", true) ->
                spotifyProvider.query(link)

            // YOUTUBE
            link.contains("youtube.com", true) || link.contains("youtu.be", true) ->
                youtubeProvider.query(link)

            // Jio Saavn
            link.contains("saavn", true) ->
                saavnProvider.query(link)

            // GAANA
            link.contains("gaana", true) ->
                gaanaProvider.query(link)

            else -> {
                null
            }
        }
        if (result != null) {
            addToDatabaseAsync(
                link,
                result.copy() // Send a copy in order to not to freeze Result itself
            )
        }
        return result
    }
    private fun addToDatabaseAsync(link: String, result: PlatformQueryResult) {
        GlobalScope.launch(dispatcherIO) {
            db?.add(
                result.folderType, result.title, link, result.coverUrl, result.trackList.size.toLong()
            )
        }
    }
}
