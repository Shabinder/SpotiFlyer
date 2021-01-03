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

package com.shabinder.spotiflyer.worker

import com.mpatric.mp3agic.ID3v1Tag
import com.mpatric.mp3agic.ID3v24Tag
import com.mpatric.mp3agic.Mp3File
import com.shabinder.spotiflyer.models.TrackDetails
import com.shabinder.spotiflyer.utils.log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileInputStream

/**
 *Modifying Mp3 com.shabinder.spotiflyer.models.gaana.Tags with MetaData!
 **/
fun setId3v1Tags(mp3File: Mp3File, track: TrackDetails): Mp3File {
    val id3v1Tag = ID3v1Tag().apply {
        artist = track.artists.joinToString(",")
        title = track.title
        album = track.albumName
        year = track.year
        comment = "Genres:${track.comment}"
    }
    mp3File.id3v1Tag = id3v1Tag
    return mp3File
}

fun setId3v2Tags(mp3file: Mp3File, track: TrackDetails,service: ForegroundService): Mp3File {
    val id3v2Tag = ID3v24Tag().apply {
        artist = track.artists.joinToString(",")
        title = track.title
        album = track.albumName
        year = track.year
        comment = "Genres:${track.comment}"
        lyrics = "Gonna Implement Soon"
        url = track.trackUrl
    }
    try{
        val bytesArray = ByteArray(track.albumArt.length().toInt())
        val fis = FileInputStream(track.albumArt)
        fis.read(bytesArray) //read file into bytes[]
        fis.close()
        id3v2Tag.setAlbumImage(bytesArray, "image/jpeg")
    }catch (e: java.io.FileNotFoundException){
        try {
            //Image Still Not Downloaded!
            //Lets Download Now and Write it into Album Art
            GlobalScope.launch {
                service.downloadAllImages(arrayListOf(track.albumArtURL, track.source.name)) {
                    val bytesArray = ByteArray(it.length().toInt())
                    val fis = FileInputStream(it)
                    fis.read(bytesArray) //read file into bytes[]
                    fis.close()
                    id3v2Tag.setAlbumImage(bytesArray, "image/jpeg")
                }
            }
        }catch (e: Exception){log("Error", "Couldn't Write Mp3 Album Art, error: ${e.stackTrace}")}
    }
    mp3file.id3v2Tag = id3v2Tag
    return mp3file
}

fun removeAllTags(mp3file: Mp3File): Mp3File {
    if (mp3file.hasId3v1Tag()) {
        mp3file.removeId3v1Tag()
    }
    if (mp3file.hasId3v2Tag()) {
        mp3file.removeId3v2Tag()
    }
    if (mp3file.hasCustomTag()) {
        mp3file.removeCustomTag()
    }
    return mp3file
}