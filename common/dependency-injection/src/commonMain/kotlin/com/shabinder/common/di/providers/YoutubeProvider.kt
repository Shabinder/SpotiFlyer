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

package com.shabinder.common.di.providers

import co.touchlab.kermit.Kermit
import com.shabinder.common.di.Dir
import com.shabinder.common.di.finalOutputDir
import com.shabinder.common.di.utils.removeIllegalChars
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.common.models.SpotiFlyerException
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import com.shabinder.common.models.spotify.Source
import io.github.shabinder.YoutubeDownloader
import io.github.shabinder.models.YoutubeVideo
import io.github.shabinder.models.formats.Format
import io.github.shabinder.models.quality.AudioQuality
import io.ktor.client.*

class YoutubeProvider(
    private val httpClient: HttpClient,
    private val logger: Kermit,
    private val dir: Dir,
) {
    val ytDownloader: YoutubeDownloader = YoutubeDownloader(
        enableCORSProxy = true,
        CORSProxyAddress = "https://cors.spotiflyer.ml/cors/"
    )

    /*
    * YT Album Art Schema
    * HI-RES Url: https://i.ytimg.com/vi/$searchId/maxresdefault.jpg"
    * Normal Url: https://i.ytimg.com/vi/$searchId/hqdefault.jpg"
    * */
    private val sampleDomain1 = "music.youtube.com"
    private val sampleDomain2 = "youtube.com"
    private val sampleDomain3 = "youtu.be"

    suspend fun query(fullLink: String): SuspendableEvent<PlatformQueryResult,Throwable> {
        val link = fullLink.removePrefix("https://").removePrefix("http://")
        if (link.contains("playlist", true) || link.contains("list", true)) {
            // Given Link is of a Playlist
            logger.i { link }
            val playlistId = link.substringAfter("?list=").substringAfter("&list=").substringBefore("&").substringBefore("?")
            return getYTPlaylist(
                playlistId
            )
        } else { // Given Link is of a Video
            var searchId = "error"
            when {
                link.contains(sampleDomain1, true) -> { // Youtube Music
                    searchId = link.substringAfterLast("/", "error").substringBefore("&").substringAfterLast("=")
                }
                link.contains(sampleDomain2, true) -> { // Standard Youtube Link
                    searchId = link.substringAfterLast("=", "error").substringBefore("&")
                }
                link.contains(sampleDomain3, true) -> { // Shortened Youtube Link
                    searchId = link.substringAfterLast("/", "error").substringBefore("&")
                }
            }
            return if (searchId != "error") {
                getYTTrack(
                    searchId
                )
            } else {
                logger.d { "Your Youtube Link is not of a Video!!" }
                SuspendableEvent.error(SpotiFlyerException.LinkInvalid(fullLink))
            }
        }
    }

    private suspend fun getYTPlaylist(
        searchId: String
    ): SuspendableEvent<PlatformQueryResult,Throwable> = SuspendableEvent {
        PlatformQueryResult(
            folderType = "",
            subFolder = "",
            title = "",
            coverUrl = "",
            trackList = listOf(),
            Source.YouTube
        ).apply {
            val playlist = ytDownloader.getPlaylist(searchId)
            val playlistDetails = playlist.details
            val name = playlistDetails.title
            subFolder = removeIllegalChars(name)
            val videos = playlist.videos

            coverUrl = "https://i.ytimg.com/vi/${
                videos.firstOrNull()?.videoId
            }/hqdefault.jpg"
            title = name

            trackList = videos.map {
                TrackDetails(
                    title = it.title ?: "N/A",
                    artists = listOf(it.author ?: "N/A"),
                    durationSec = it.lengthSeconds,
                    albumArtPath = dir.imageCacheDir() + it.videoId + ".jpeg",
                    source = Source.YouTube,
                    albumArtURL = "https://i.ytimg.com/vi/${it.videoId}/hqdefault.jpg",
                    downloaded = if (dir.isPresent(
                            dir.finalOutputDir(
                                itemName = it.title ?: "N/A",
                                type = folderType,
                                subFolder = subFolder,
                                dir.defaultDir()
                            )
                        )
                    )
                        DownloadStatus.Downloaded
                    else {
                        DownloadStatus.NotDownloaded
                    },
                    outputFilePath = dir.finalOutputDir(it.title ?: "N/A", folderType, subFolder, dir.defaultDir()/*,".m4a"*/),
                    videoID = it.videoId
                )
            }
        }
    }

    @Suppress("DefaultLocale")
    private suspend fun getYTTrack(
        searchId: String,
    ): SuspendableEvent<PlatformQueryResult,Throwable> = SuspendableEvent {
        PlatformQueryResult(
            folderType = "",
            subFolder = "",
            title = "",
            coverUrl = "",
            trackList = listOf(),
            Source.YouTube
        ).apply {
            val video = ytDownloader.getVideo(searchId)
            coverUrl = "https://i.ytimg.com/vi/$searchId/hqdefault.jpg"
            val detail = video.videoDetails
            val name = detail.title?.replace(detail.author?.uppercase() ?: "", "", true)
                ?: detail.title ?: ""
            // logger.i{ detail.toString() }
            trackList = listOf(
                TrackDetails(
                    title = name,
                    artists = listOf(detail.author ?: "N/A"),
                    durationSec = detail.lengthSeconds,
                    albumArtPath = dir.imageCacheDir() + "$searchId.jpeg",
                    source = Source.YouTube,
                    albumArtURL = "https://i.ytimg.com/vi/$searchId/hqdefault.jpg",
                    downloaded = if (dir.isPresent(
                            dir.finalOutputDir(
                                itemName = name,
                                type = folderType,
                                subFolder = subFolder,
                                defaultDir = dir.defaultDir()
                            )
                        )
                    )
                        DownloadStatus.Downloaded
                    else {
                        DownloadStatus.NotDownloaded
                    },
                    outputFilePath = dir.finalOutputDir(name, folderType, subFolder, dir.defaultDir()/*,".m4a"*/),
                    videoID = searchId
                )
            )
            title = name
        }
    }
}

fun YoutubeVideo.get(): Format? {
    return getAudioWithQuality(AudioQuality.high).getOrNull(0)
        ?: getAudioWithQuality(AudioQuality.medium).getOrNull(0)
        ?: getAudioWithQuality(AudioQuality.low).getOrNull(0)
}
