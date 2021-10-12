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

package com.shabinder.common.providers.youtube

import co.touchlab.kermit.Kermit
import com.shabinder.common.core_components.file_manager.FileManager
import com.shabinder.common.core_components.file_manager.finalOutputDir
import com.shabinder.common.core_components.file_manager.getImageCachePath
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.common.models.SpotiFlyerException
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import com.shabinder.common.models.spotify.Source
import com.shabinder.common.utils.removeIllegalChars
import io.github.shabinder.YoutubeDownloader
import io.github.shabinder.models.Extension
import io.github.shabinder.models.YoutubeVideo
import io.github.shabinder.models.formats.Format
import io.github.shabinder.models.quality.AudioQuality
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpStatement
import io.ktor.http.HttpStatusCode
import com.shabinder.common.models.AudioQuality as Quality

class YoutubeProvider(
    private val httpClient: HttpClient,
    private val logger: Kermit,
    private val fileManager: FileManager,
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

    suspend fun query(fullLink: String): SuspendableEvent<PlatformQueryResult, Throwable> {
        val link = fullLink.removePrefix("https://").removePrefix("http://")
        if (link.contains("playlist", true) || link.contains("list", true)) {
            // Given Link is of a Playlist
            logger.i { link }
            val playlistId =
                link.substringAfter("?list=").substringAfter("&list=").substringBefore("&").substringBefore("?")
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
    ): SuspendableEvent<PlatformQueryResult, Throwable> = SuspendableEvent {
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
                val imageURL = "https://i.ytimg.com/vi/${it.videoId}/hqdefault.jpg"
                TrackDetails(
                    title = it.title ?: "N/A",
                    artists = listOf(it.author ?: "N/A"),
                    durationSec = it.lengthSeconds,
                    albumArtPath = fileManager.getImageCachePath(imageURL),
                    source = Source.YouTube,
                    albumArtURL = imageURL,
                    downloaded = if (fileManager.isPresent(
                            fileManager.finalOutputDir(
                                itemName = it.title ?: "N/A",
                                type = folderType,
                                subFolder = subFolder,
                                fileManager.defaultDir()
                            )
                        )
                    )
                        DownloadStatus.Downloaded
                    else {
                        DownloadStatus.NotDownloaded
                    },
                    outputFilePath = fileManager.finalOutputDir(
                        it.title ?: "N/A",
                        folderType,
                        subFolder,
                        fileManager.defaultDir()/*,".m4a"*/
                    ),
                    videoID = it.videoId
                )
            }
        }
    }

    @Suppress("DefaultLocale")
    private suspend fun getYTTrack(
        searchId: String,
    ): SuspendableEvent<PlatformQueryResult, Throwable> = SuspendableEvent {
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
            val name = detail.title?.replace(detail.author?.toUpperCase() ?: "", "", true)
                ?: detail.title ?: ""
            // logger.i{ detail.toString() }
            trackList = listOf(
                TrackDetails(
                    title = name,
                    artists = listOf(detail.author ?: "N/A"),
                    durationSec = detail.lengthSeconds,
                    albumArtPath = fileManager.getImageCachePath(coverUrl),
                    source = Source.YouTube,
                    albumArtURL = coverUrl,
                    downloaded = if (fileManager.isPresent(
                            fileManager.finalOutputDir(
                                itemName = name,
                                type = folderType,
                                subFolder = subFolder,
                                defaultDir = fileManager.defaultDir()
                            )
                        )
                    )
                        DownloadStatus.Downloaded
                    else {
                        DownloadStatus.NotDownloaded
                    },
                    outputFilePath = fileManager.finalOutputDir(
                        name,
                        folderType,
                        subFolder,
                        fileManager.defaultDir()/*,".m4a"*/
                    ),
                    videoID = searchId
                )
            )
            title = name
        }
    }

    suspend fun fetchVideoM4aLink(videoId: String, retryCount: Int = 3): Pair<String, Quality> {
        @Suppress("NAME_SHADOWING")
        var retryCount = retryCount
        var validM4aLink: String? = null
        var audioQuality: Quality = Quality.KBPS128

        val ex = SpotiFlyerException.DownloadLinkFetchFailed("Manual Extraction Failed for VideoID: $videoId")
        while (validM4aLink.isNullOrEmpty() && retryCount > 0) {
            val m4aLink = ytDownloader.getVideo(videoId).getM4aLink()?.also {
                audioQuality =
                    if (it.bitrate > 160_000) Quality.KBPS192 else Quality.KBPS128
            }?.url
                ?: throw ex

            if (validateLink(m4aLink)) {
                validM4aLink = m4aLink
            }
            retryCount--
        }

        if (validM4aLink.isNullOrBlank())
            throw ex

        return validM4aLink to audioQuality
    }

    private suspend fun validateLink(link: String): Boolean {
        var status = HttpStatusCode.BadRequest
        httpClient.get<HttpStatement>(link).execute { res -> status = res.status }
        return status == HttpStatusCode.OK
    }

    private fun YoutubeVideo.getM4aLink(): Format? {
        return getAudioWithQuality(AudioQuality.high).firstOrNull { it.extension == Extension.M4A }
            ?: getAudioWithQuality(AudioQuality.medium).firstOrNull { it.extension == Extension.M4A }
            ?: getAudioWithQuality(AudioQuality.low).firstOrNull { it.extension == Extension.M4A }
    }
}

