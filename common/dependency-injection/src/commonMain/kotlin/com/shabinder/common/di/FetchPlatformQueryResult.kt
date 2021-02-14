package com.shabinder.common.di

import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.common.database.DownloadRecordDatabaseQueries
import com.shabinder.common.di.providers.GaanaProvider
import com.shabinder.common.di.providers.SpotifyProvider
import com.shabinder.common.di.providers.YoutubeMusic
import com.shabinder.database.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchPlatformQueryResult(
    private val gaanaProvider: GaanaProvider,
    private val spotifyProvider: SpotifyProvider,
    private val youtubeProvider: YoutubeProvider,
    val youtubeMusic: YoutubeMusic,
    private val database: Database
) {
    private val db:DownloadRecordDatabaseQueries
        get() = database.downloadRecordDatabaseQueries

    suspend fun query(link:String): PlatformQueryResult?{
        val result = when{
            //SPOTIFY
            link.contains("spotify",true) ->
                spotifyProvider.query(link)

            //YOUTUBE
            link.contains("youtube.com",true) || link.contains("youtu.be",true) ->
                youtubeProvider.query(link)

            //GAANA
            link.contains("gaana",true) ->
                gaanaProvider.query(link)

            else -> {
                null
            }
        }
        result?.run {
            withContext(Dispatchers.Default){
                db.add(
                    folderType, title, link, coverUrl, trackList.size.toLong()
                )
            }
        }
        return result
    }
}