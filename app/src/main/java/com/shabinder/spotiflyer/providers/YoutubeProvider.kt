/*
 * Copyright (c)  2021  Shabinder Singh
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer.providers

import android.annotation.SuppressLint
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.spotiflyer.database.DownloadRecord
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.models.PlatformQueryResult
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.utils.log
import com.shabinder.spotiflyer.utils.removeIllegalChars
import com.shabinder.spotiflyer.utils.showDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoutubeProvider @Inject constructor(
    private val ytDownloader: YoutubeDownloader,
): BaseProvider() {
    /*
    * YT Album Art Schema
    * HI-RES Url: https://i.ytimg.com/vi/$searchId/maxresdefault.jpg"
    * Normal Url: https://i.ytimg.com/vi/$searchId/hqdefault.jpg"
    * */
    private val sampleDomain1 = "music.youtube.com"
    private val sampleDomain2 = "youtube.com"
    private val sampleDomain3 = "youtu.be"

    override suspend fun query(fullLink: String): PlatformQueryResult?{
        val link = fullLink.removePrefix("https://").removePrefix("http://")
        if(link.contains("playlist",true) || link.contains("list",true)){
            // Given Link is of a Playlist
            log("YT Play",link)
            val playlistId = link.substringAfter("?list=").substringAfter("&list=").substringBefore("&").substringBefore("?")
            return getYTPlaylist(
                playlistId
            )
        }else{//Given Link is of a Video
            var searchId = "error"
            when{
                link.contains(sampleDomain1,true) -> {//Youtube Music
                    searchId = link.substringAfterLast("/","error").substringBefore("&").substringAfterLast("=")
                }
                link.contains(sampleDomain2,true) -> {//Standard Youtube Link
                    searchId =  link.substringAfterLast("=","error").substringBefore("&")
                }
                link.contains(sampleDomain3,true) -> {//Shortened Youtube Link
                    searchId = link.substringAfterLast("/","error").substringBefore("&")
                }
            }
            return if(searchId != "error") {
                getYTTrack(
                    searchId
                )
            }else{
                showDialog("Your Youtube Link is not of a Video!!")
                null
            }
        }
    }

    private suspend fun getYTPlaylist(
        searchId: String
    ):PlatformQueryResult?{
        val result = PlatformQueryResult(
            folderType = "",
            subFolder = "",
            title = "",
            coverUrl = "",
            trackList = listOf(),
            Source.YouTube
        )
        with(result) {
            try {
                log("YT Playlist", searchId)
                val playlist = ytDownloader.getPlaylist(searchId)
                val playlistDetails = playlist.details()
                val name = playlistDetails.title()
                subFolder = removeIllegalChars(name)
                val videos = playlist.videos()

                coverUrl = "https://i.ytimg.com/vi/${
                    videos.firstOrNull()?.videoId()
                }/hqdefault.jpg"
                title = name

                trackList = videos.map {
                    TrackDetails(
                        title = it.title(),
                        artists = listOf(it.author().toString()),
                        durationSec = it.lengthSeconds(),
                        albumArt = File(imageDir + it.videoId() + ".jpeg"),
                        source = Source.YouTube,
                        albumArtURL = "https://i.ytimg.com/vi/${it.videoId()}/hqdefault.jpg",
                        downloaded = if (File(
                                finalOutputDir(
                                    itemName = it.title(),
                                    type = folderType,
                                    subFolder = subFolder,
                                    defaultDir
                                )
                            ).exists()
                        )
                            DownloadStatus.Downloaded
                        else {
                            DownloadStatus.NotDownloaded
                        },
                        outputFile = finalOutputDir(it.title(), folderType, subFolder, defaultDir,".m4a"),
                        videoID = it.videoId()
                    )
                }

                withContext(Dispatchers.IO) {
                    databaseDAO.insert(
                        DownloadRecord(
                            type = "PlayList",
                            name = if (name.length > 17) {
                                "${name.subSequence(0, 16)}..."
                            } else {
                                name
                            },
                            link = "https://www.youtube.com/playlist?list=$searchId",
                            coverUrl = "https://i.ytimg.com/vi/${
                                videos.firstOrNull()?.videoId()
                            }/hqdefault.jpg",
                            totalFiles = videos.size,
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showDialog("An Error Occurred While Processing!")
            }
        }
        return if(result.title.isNotBlank()) result
        else null
    }

    @SuppressLint("DefaultLocale")
    private suspend fun getYTTrack(
        searchId:String,
    ):PlatformQueryResult? {
        val result = PlatformQueryResult(
            folderType = "",
            subFolder = "",
            title = "",
            coverUrl = "",
            trackList = listOf(),
            Source.YouTube
        )
        with(result) {
            try {
                log("YT Video", searchId)
                val video = ytDownloader.getVideo(searchId)
                coverUrl = "https://i.ytimg.com/vi/$searchId/hqdefault.jpg"
                val detail = video?.details()
                val name = detail?.title()?.replace(detail.author()!!.toUpperCase(), "", true)
                    ?: detail?.title() ?: ""
                log("YT View Model", detail.toString())
                trackList = listOf(
                    TrackDetails(
                        title = name,
                        artists = listOf(detail?.author().toString()),
                        durationSec = detail?.lengthSeconds() ?: 0,
                        albumArt = File(imageDir, "$searchId.jpeg"),
                        source = Source.YouTube,
                        albumArtURL = "https://i.ytimg.com/vi/$searchId/hqdefault.jpg",
                        downloaded = if (File(
                                finalOutputDir(
                                    itemName = name,
                                    type = folderType,
                                    subFolder = subFolder,
                                    defaultDir = defaultDir
                                )
                            ).exists()
                        )
                            DownloadStatus.Downloaded
                        else {
                            DownloadStatus.NotDownloaded
                        },
                        outputFile = finalOutputDir(name, folderType, subFolder, defaultDir,".m4a"),
                        videoID = searchId
                    )
                )
                title = name

                withContext(Dispatchers.IO) {
                    databaseDAO.insert(
                        DownloadRecord(
                            type = "Track",
                            name = if (name.length > 17) {
                                "${name.subSequence(0, 16)}..."
                            } else {
                                name
                            },
                            link = "https://www.youtube.com/watch?v=$searchId",
                            coverUrl = "https://i.ytimg.com/vi/$searchId/hqdefault.jpg",
                            totalFiles = 1,
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showDialog("An Error Occurred While Processing!,$searchId")
            }
        }
        return if(result.title.isNotBlank()) result
        else null
    }
}