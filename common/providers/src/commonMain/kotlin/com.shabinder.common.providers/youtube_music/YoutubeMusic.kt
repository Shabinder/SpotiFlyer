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

package com.shabinder.common.providers.youtube_music

import co.touchlab.kermit.Kermit
import com.shabinder.common.core_components.file_manager.FileManager
import com.shabinder.common.models.AudioFormat
import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.SpotiFlyerException
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.YoutubeTrack
import com.shabinder.common.models.corsApi
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import com.shabinder.common.models.event.coroutines.flatMap
import com.shabinder.common.models.event.coroutines.flatMapError
import com.shabinder.common.models.event.coroutines.map
import com.shabinder.common.models.event.coroutines.onFailure
import com.shabinder.common.providers.youtube.YoutubeProvider
import com.shabinder.common.providers.youtube_to_mp3.requests.YoutubeMp3
import com.shabinder.common.utils.appendPadded
import io.github.shabinder.fuzzywuzzy.diffutils.FuzzySearch
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlin.collections.set
import kotlin.math.absoluteValue

class YoutubeMusic constructor(
    private val logger: Kermit,
    private val httpClient: HttpClient,
    private val youtubeProvider: YoutubeProvider,
    private val youtubeMp3: YoutubeMp3,
    private val fileManager: FileManager,
) {
    companion object {
        const val apiKey = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30"
        const val tag = "YT Music"
    }

    // Get Downloadable Link
    suspend fun findSongDownloadURLYT(
        trackDetails: TrackDetails,
        preferredQuality: AudioQuality = fileManager.preferenceManager.audioQuality,
        errorReportBuilder: StringBuilder? = null
    ): SuspendableEvent<Triple<String, AudioQuality, AudioFormat>, Throwable> {
        return getYTIDBestMatch(trackDetails).flatMap { videoID ->
            // As YT compress Audio hence there is no benefit of quality for more than 192
            val optimalQuality =
                if ((preferredQuality.kbps.toIntOrNull()
                        ?: 0) > 192
                ) AudioQuality.KBPS192 else preferredQuality
            // 1 Try getting Link from Yt1s
            youtubeMp3.getMp3DownloadLink(videoID, optimalQuality).map {
                Triple(it, optimalQuality, AudioFormat.MP3)
            }.flatMapError {
                errorReportBuilder?.appendPadded(
                    "Yt1sMp3 Failed for $videoID:", it.stackTraceToString()
                )
                // 2 if Yt1s failed , Extract Manually
                errorReportBuilder?.appendPadded("Extracting Manually...")
                SuspendableEvent {
                    youtubeProvider.fetchVideoM4aLink(videoID)
                }.onFailure { throwable ->
                    errorReportBuilder?.appendPadded("YT Manual Extraction Failed!", throwable.stackTraceToString())
                }.map { (URL, quality) ->
                    Triple(URL, quality, AudioFormat.MP4)
                }
            }
        }
    }

    private suspend fun getYTIDBestMatch(
        trackDetails: TrackDetails
    ): SuspendableEvent<String, Throwable> =
        getYTTracks("${trackDetails.title} - ${trackDetails.artists.joinToString(",")}").map { matchList ->
            sortByBestMatch(
                matchList,
                trackName = trackDetails.title,
                trackArtists = trackDetails.artists,
                trackDurationSec = trackDetails.durationSec
            ).also { logger.d("YT-M Matches:") { it.entries.joinToString("\n") { "${it.key} --- ${it.value}" } } }.keys.firstOrNull()
                ?: throw SpotiFlyerException.NoMatchFound(trackDetails.title)
        }

    private suspend fun getYTTracks(query: String): SuspendableEvent<List<YoutubeTrack>, Throwable> =
        getYoutubeMusicResponse(query).map { youtubeResponseData ->
            val youtubeTracks = mutableListOf<YoutubeTrack>()
            val responseObj = Json.parseToJsonElement(youtubeResponseData)
            // logger.i { "Youtube Music Response Received" }
            val contentBlocks = responseObj.jsonObject["contents"]
                ?.jsonObject?.get("tabbedSearchResultsRenderer")
                ?.jsonObject?.get("tabs")?.jsonArray?.get(0)
                ?.jsonObject?.get("tabRenderer")
                ?.jsonObject?.get("content")
                ?.jsonObject?.get("sectionListRenderer")
                ?.jsonObject?.get("contents")?.jsonArray

            val resultBlocks = mutableListOf<JsonArray>()
            if (contentBlocks != null) {
                for (cBlock in contentBlocks) {
                    /**
                     *Ignore user-suggestion
                     *The 'itemSectionRenderer' field is for user notices (stuff like - 'showing
                     *results for xyz, search for abc instead') we have no use for them, the for
                     *loop below if throw a keyError if we don't ignore them
                     */
                    if (cBlock.jsonObject.containsKey("itemSectionRenderer")) {
                        continue
                    }

                    for (
                    contents in cBlock.jsonObject["musicShelfRenderer"]?.jsonObject?.get("contents")?.jsonArray
                        ?: listOf()
                    ) {
                        /**
                         *  apparently content Blocks without an 'overlay' field don't have linkBlocks
                         *  I have no clue what they are and why there even exist
                         *
                        if(!contents.containsKey("overlay")){
                        println(contents)
                        continue
                        TODO check and correct
                        }*/

                        val result = contents.jsonObject["musicResponsiveListItemRenderer"]
                            ?.jsonObject?.get("flexColumns")?.jsonArray

                        // Add the linkBlock
                        val linkBlock = contents.jsonObject["musicResponsiveListItemRenderer"]
                            ?.jsonObject?.get("overlay")
                            ?.jsonObject?.get("musicItemThumbnailOverlayRenderer")
                            ?.jsonObject?.get("content")
                            ?.jsonObject?.get("musicPlayButtonRenderer")
                            ?.jsonObject?.get("playNavigationEndpoint")

                        // detailsBlock is always a list, so we just append the linkBlock to it
                        // instead of carrying along all the other junk from "musicResponsiveListItemRenderer"
                        val finalResult = buildJsonArray {
                            result?.let { add(it) }
                            linkBlock?.let { add(it) }
                        }
                        resultBlocks.add(finalResult)
                    }
                }

                /* We only need results that are Songs or Videos, so we filter out the rest, since
                ! Songs and Videos are supplied with different details, extracting all details from
                ! both is just carrying on redundant data, so we also have to selectively extract
                ! relevant details. What you need to know to understand how we do that here:
                !
                ! Songs details are ALWAYS in the following order:
                !       0 - Name
                !       1 - Type (Song)
                !       2 - com.shabinder.spotiflyer.models.gaana.Artist
                !       3 - Album
                !       4 - Duration (mm:ss)
                !
                ! Video details are ALWAYS in the following order:
                !       0 - Name
                !       1 - Type (Video)
                !       2 - Channel
                !       3 - Viewers
                !       4 - Duration (hh:mm:ss)
                !
                ! We blindly gather all the details we get our hands on, then
                ! cherry-pick the details we need based on  their index numbers,
                ! we do so only if their Type is 'Song' or 'Video
                */

                for (result in resultBlocks) {

                    // Blindly gather available details
                    val availableDetails = mutableListOf<String>()

                    /*
                    Filter Out dummies here itself
                    ! 'musicResponsiveListItemFlexColumnRenderer' should have more that one
                    ! sub-block, if not it is a dummy, why does the YTM response contain dummies?
                    ! I have no clue. We skip these.

                    ! Remember that we appended the linkBlock to result, treating that like the
                    ! other constituents of a result block will lead to errors, hence the 'in
                    ! result[:-1] ,i.e., skip last element in array '
                    */
                    for (detailArray in result.subList(0, result.size - 1)) {
                        for (detail in detailArray.jsonArray) {
                            if ((detail.jsonObject["musicResponsiveListItemFlexColumnRenderer"]?.jsonObject?.size
                                    ?: 0) < 2
                            ) continue

                            // if not a dummy, collect All Variables
                            val details =
                                detail.jsonObject["musicResponsiveListItemFlexColumnRenderer"]
                                    ?.jsonObject?.get("text")
                                    ?.jsonObject?.get("runs")?.jsonArray ?: listOf()

                            for (d in details) {
                                d.jsonObject["text"]?.jsonPrimitive?.contentOrNull?.let {
                                    if (it != " • ") {
                                        availableDetails.add(it)
                                    }
                                }
                            }
                        }
                    }
                    // logger.d("YT Music details"){availableDetails.toString()}
                    /*
                    ! Filter Out non-Song/Video results and incomplete results here itself
                    ! From what we know about detail order, note that [1] - indicate result type
                    */
                    if (availableDetails.size == 5 && availableDetails[1] in listOf(
                            "Song",
                            "Video"
                        )
                    ) {

                        // skip if result is in hours instead of minutes (no song is that long)
                        if (availableDetails[4].split(':').size != 2) continue

                        /*
                        ! grab Video ID
                        ! this is nested as [playlistEndpoint/watchEndpoint][videoId/playlistId/...]
                        ! so hardcoding the dict keys for data look up is an ardours process, since
                        ! the sub-block pattern is fixed even though the key isn't, we just
                        ! reference the dict keys by index
                        */

                        val videoId: String? =
                            result.last().jsonObject["watchEndpoint"]?.jsonObject?.get("videoId")?.jsonPrimitive?.content
                        val ytTrack = YoutubeTrack(
                            name = availableDetails[0],
                            type = availableDetails[1],
                            artist = availableDetails[2],
                            duration = availableDetails[4],
                            videoId = videoId
                        )
                        youtubeTracks.add(ytTrack)
                    }
                }
            }
            // logger.d {youtubeTracks.joinToString("\n")}
            youtubeTracks
        }

    fun sortByBestMatch(
        ytTracks: List<YoutubeTrack>,
        trackName: String,
        trackArtists: List<String>,
        trackDurationSec: Int,
    ): Map<String, Float> {
        /*
        * "linksWithMatchValue" is map with Youtube VideoID and its rating/match with 100 as Max Value
        **/
        val linksWithMatchValue = mutableMapOf<String, Float>()

        for (result in ytTracks) {

            // LoweCasing Name to match Properly
            // most song results on youtube go by $artist - $songName or artist1/artist2
            var hasCommonWord = false

            val resultName = result.name?.toLowerCase()?.replace("-", " ")?.replace("/", " ") ?: ""
            val trackNameWords = trackName.toLowerCase().split(" ")

            for (nameWord in trackNameWords) {
                if (nameWord.isNotBlank() && FuzzySearch.partialRatio(
                        nameWord,
                        resultName
                    ) > 85
                ) hasCommonWord = true
            }

            // Skip this Result if No Word is Common in Name
            if (!hasCommonWord) {
                logger.d("YT Api Removing No common Word") { result.toString() }
                continue
            }

            // Find artist match
            // Will Be Using Fuzzy Search Because YT Spelling might be mucked up
            // match  = (no of artist names in result) / (no. of artist names on spotify) * 100
            var artistMatchNumber = 0F

            if (result.type == "Song") {
                for (artist in trackArtists) {
                    if (FuzzySearch.ratio(
                            artist.toLowerCase(),
                            result.artist?.toLowerCase() ?: ""
                        ) > 85
                    )
                        artistMatchNumber++
                }
            } else { // i.e. is a Video
                for (artist in trackArtists) {
                    if (FuzzySearch.partialRatio(
                            artist.toLowerCase(),
                            result.name?.toLowerCase() ?: ""
                        ) > 85
                    )
                        artistMatchNumber++
                }
            }

            if (artistMatchNumber == 0F) {
                logger.d { "YT Api Removing Artist Match 0:   $result" }
                continue
            }

            val artistMatch = (artistMatchNumber / trackArtists.size.toFloat()) * 100F

            // Duration Match
            /*! time match = 100 - (delta(duration)**2 / original duration * 100)
            ! difference in song duration (delta) is usually of the magnitude of a few
            ! seconds, we need to amplify the delta if it is to have any meaningful impact
            ! wen we calculate the avg match value*/
            val difference: Float = result.duration?.split(":")?.get(0)?.toFloat()?.times(60)
                ?.plus(result.duration?.split(":")?.get(1)?.toFloat() ?: 0F)
                ?.minus(trackDurationSec)?.absoluteValue ?: 0F
            val nonMatchValue: Float = ((difference * difference) / trackDurationSec.toFloat())
            val durationMatch: Float = 100 - (nonMatchValue * 100F)

            val avgMatch: Float = (artistMatch + durationMatch) / 2F
            linksWithMatchValue[result.videoId.toString()] = avgMatch
        }
        // logger.d("YT Api Result"){"$trackName - $linksWithMatchValue"}
        return linksWithMatchValue.toList().sortedByDescending { it.second }.toMap().also {
            logger.d(tag) { "Match Found for $trackName - ${!it.isNullOrEmpty()}  ${it.keys.firstOrNull() ?: ""}" }
        }
    }

    private suspend fun getYoutubeMusicResponse(query: String): SuspendableEvent<String, Throwable> =
        SuspendableEvent {
            httpClient.post("${corsApi}https://music.youtube.com/youtubei/v1/search?alt=json&key=$apiKey") {
                headers {
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    append("referer", "https://music.youtube.com/search")
                }
                body = buildJsonObject {
                    putJsonObject("context") {
                        putJsonObject("client") {
                            put("clientName", "WEB_REMIX")
                            put("clientVersion", "0.1")
                        }
                    }
                    put("query", query)
                }
            }
        }
}
