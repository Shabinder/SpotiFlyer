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

package com.shabinder.common.core_components

import com.mpatric.mp3agic.ID3v1Tag
import com.mpatric.mp3agic.ID3v24Tag
import com.mpatric.mp3agic.Mp3File
import com.shabinder.common.core_components.file_manager.downloadFile
import com.shabinder.common.models.DownloadResult
import com.shabinder.common.models.TrackDetails
import kotlinx.coroutines.flow.collect
import java.io.File
import java.io.FileInputStream

fun Mp3File.removeAllTags(): Mp3File {
    if (hasId3v1Tag()) removeId3v1Tag()
    if (hasId3v2Tag()) removeId3v2Tag()
    if (hasCustomTag()) removeCustomTag()
    return this
}

/**
 * Modifying Mp3 with MetaData!
 **/
fun Mp3File.setId3v1Tags(track: TrackDetails): Mp3File {
    val id3v1Tag = ID3v1Tag().apply {
        artist = track.artists.joinToString(",")
        title = track.title
        album = track.albumName
        year = track.year
        comment = "Genres:${track.comment}"
    }
    this.id3v1Tag = id3v1Tag
    return this
}


@Suppress("BlockingMethodInNonBlockingContext")
suspend fun Mp3File.setId3v2TagsAndSaveFile(track: TrackDetails, outputFilePath: String? = null) {
    val id3v2Tag = ID3v24Tag().apply {
        albumArtist = track.albumArtists.joinToString(", ")
        artist = track.artists.joinToString(", ")
        title = track.title
        album = track.albumName
        year = track.year

        genreDescription = "Genre: " + track.genre.joinToString(", ")
        comment = track.comment
        lyrics = track.lyrics ?: ""
        url = track.trackUrl
        if (track.trackNumber != null)
            this.track = track.trackNumber.toString()
    }
    try {
        val art = File(track.albumArtPath)
        val bytesArray = ByteArray(art.length().toInt())
        val fis = FileInputStream(art)
        fis.read(bytesArray) // read file into bytes[]
        fis.close()
        id3v2Tag.setAlbumImage(bytesArray, "image/jpeg")
        this.id3v2Tag = id3v2Tag
        saveFile(outputFilePath ?: track.outputFilePath)
    } catch (e: java.io.FileNotFoundException) {
        try {
            // Image Still Not Downloaded!
            // Lets Download Now and Write it into Album Art
            downloadFile(track.albumArtURL).collect {
                when (it) {
                    is DownloadResult.Error -> {} // Error
                    is DownloadResult.Success -> {
                        id3v2Tag.setAlbumImage(it.byteArray, "image/jpeg")
                        this.id3v2Tag = id3v2Tag
                        saveFile(outputFilePath ?: track.outputFilePath)
                    }
                    is DownloadResult.Progress -> {} // Nothing for Now , no progress bar to show
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun Mp3File.saveFile(filePath: String) {
    save(filePath.substringBeforeLast('.') + ".tagged.mp3")

    val oldFile = File(filePath)
    oldFile.delete()

    val newFile = File((filePath.substringBeforeLast('.') + ".tagged.mp3"))
    newFile.renameTo(File(filePath.substringBeforeLast('.') + ".mp3"))
}
