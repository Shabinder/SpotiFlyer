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

package com.shabinder.spotiflyer.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
data class DownloadObject(
    var trackDetails: TrackDetails,
    var ytVideoId:String,
    var outputFile:String
):Parcelable

@Parcelize
data class TrackDetails(
    var title:String,
    var artists:List<String>,
    var durationSec:Int,
    var albumName:String?=null,
    var year:String?=null,
    var comment:String?=null,
    var lyrics:String?=null,
    var trackUrl:String?=null,
    var albumArt: File,
    var source:Source,
    var downloaded:DownloadStatus = DownloadStatus.NotDownloaded
):Parcelable

enum class DownloadStatus{
    Downloaded,
    Downloading,
    NotDownloaded
}