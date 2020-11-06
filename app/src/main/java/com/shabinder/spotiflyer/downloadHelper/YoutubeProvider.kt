/*
 * Copyright (C)  2020  Shabinder Singh
 *
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer.downloadHelper

import android.util.Log
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.spotiflyer.downloadHelper.SpotifyDownloadHelper.downloadFile
import com.shabinder.spotiflyer.downloadHelper.SpotifyDownloadHelper.notFound
import com.shabinder.spotiflyer.models.Track
import com.shabinder.spotiflyer.models.YoutubeTrack

/*
* Thanks and credits To https://github.com/spotDL/spotify-downloader
* */
fun getYTLink(type:String,
              subFolder:String?,
              ytDownloader: YoutubeDownloader?,
              response: String,
              track: Track
){
    //TODO Download File
    val youtubeTracks = mutableListOf<YoutubeTrack>()
    val parser: Parser = Parser.default()
    val stringBuilder: StringBuilder = StringBuilder(response)
    val responseObj: JsonObject = parser.parse(stringBuilder) as JsonObject
    val contentBlocks = responseObj.obj("contents")?.obj("sectionListRenderer")?.array<JsonObject>("contents")
    val resultBlocks = mutableListOf<JsonArray<JsonObject>>()
    if (contentBlocks != null) {
        Log.i("Total Content Blocks:", contentBlocks.size.toString())
        for (cBlock in contentBlocks){
            /**
             *Ignore user-suggestion
             *The 'itemSectionRenderer' field is for user notices (stuff like - 'showing
             *results for xyz, search for abc instead') we have no use for them, the for
             *loop below if throw a keyError if we don't ignore them
             */
            if(cBlock.containsKey("itemSectionRenderer")){
                continue
            }

            for(contents in cBlock.obj("musicShelfRenderer")?.array<JsonObject>("contents") ?: listOf()){
                /**
                 *  apparently content Blocks without an 'overlay' field don't have linkBlocks
                 *  I have no clue what they are and why there even exist
                 *
                if(!contents.containsKey("overlay")){
                println(contents)
                continue
                TODO check and correct
                }*/

                val result = contents.obj("musicResponsiveListItemRenderer")
                    ?.array<JsonObject>("flexColumns")

                //Add the linkBlock
                val linkBlock = contents.obj("musicResponsiveListItemRenderer")
                    ?.obj("overlay")
                    ?.obj("musicItemThumbnailOverlayRenderer")
                    ?.obj("content")
                    ?.obj("musicPlayButtonRenderer")
                    ?.obj("playNavigationEndpoint")

                // detailsBlock is always a list, so we just append the linkBlock to it
                // instead of carrying along all the other junk from "musicResponsiveListItemRenderer"
                linkBlock?.let { result?.add(it) }
                result?.let { resultBlocks.add(it) }
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
        !       2 - Artist
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
        ! cherrypick the details we need based on  their index numbers,
        ! we do so only if their Type is 'Song' or 'Video
        */

        val simplifiedResults = mutableListOf<JsonObject>()

        for(result in resultBlocks){

            // Blindly gather available details
            val availableDetails = mutableListOf<String>()

            /*
            Filter Out dummies here itself
            ! 'musicResponsiveListItemFlexColumnRenderer' should have more that one
            ! sub-block, if not its a dummy, why does the YTM response contain dummies?
            ! I have no clue. We skip these.

            ! Remember that we appended the linkBlock to result, treating that like the
            ! other constituents of a result block will lead to errors, hence the 'in
            ! result[:-1] ,i.e., skip last element in array '
            */
            for(detail in result.subList(0,result.size-2)){
                if(detail.obj("musicResponsiveListItemFlexColumnRenderer")?.size!! < 2) continue

                // if not a dummy, collect All Variables
                detail.obj("musicResponsiveListItemFlexColumnRenderer")
                    ?.obj("text")
                    ?.array<JsonObject>("runs")?.get(0)?.get("text")?.let {
                        availableDetails.add(
                            it.toString()
                        )
                    }
            }

            /*
            ! Filter Out non-Song/Video results and incomplete results here itself
            ! From what we know about detail order, note that [1] - indicate result type
            */
            if ( availableDetails.size > 1 && availableDetails[1] in listOf("Song","Video") ){

                // skip if result is in hours instead of minutes (no song is that long)
//                if(availableDetails[4].split(':').size != 2) continue TODO

                /*
                ! grab position of result
                ! This helps for those oddball cases where 2+ results are rated equally,
                ! lower position --> better match
                */
                val resultPosition = resultBlocks.indexOf(result)

                /*
                ! grab Video ID
                ! this is nested as [playlistEndpoint/watchEndpoint][videoId/playlistId/...]
                ! so hardcoding the dict keys for data look up is an ardours process, since
                ! the sub-block pattern is fixed even though the key isn't, we just
                ! reference the dict keys by index
                */

                val videoId:String = result.last().obj("watchEndpoint")?.get("videoId") as String
                val ytTrack = YoutubeTrack(
                    name = availableDetails[0],
                    type = availableDetails[1],
                    artist = availableDetails[2],
                    videoId = videoId
                )
                youtubeTracks.add(ytTrack)
            }
        }
    }
    //Songs First, Videos Later
    youtubeTracks.sortWith { o1: YoutubeTrack, o2: YoutubeTrack -> o1.type.toString().compareTo(o2.type.toString()) }

    if(youtubeTracks.firstOrNull()?.videoId.isNullOrBlank()) notFound++
    else downloadFile(
        subFolder,
        type,
        track,
        ytDownloader,
        id = youtubeTracks[0].videoId.toString()
    )
    Log.i("DHelper YT ID", youtubeTracks.firstOrNull()?.videoId ?: "Not Found")
    SpotifyDownloadHelper.updateStatusBar()
}
