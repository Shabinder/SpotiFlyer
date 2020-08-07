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

package com.shabinder.spotiflyer.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "download_record_table",
    indices = [Index(value = ["id","link"], unique = true)]
)
data class DownloadRecord(

    @PrimaryKey(autoGenerate = true)
    var id:Int = 0,

    @ColumnInfo(name = "type")
    var type:String,

    @ColumnInfo(name = "name")
    var name:String,

    @ColumnInfo(name = "link")
    var link:String,

    @ColumnInfo(name = "coverUrl")
    var coverUrl:String,

    @ColumnInfo(name = "totalFiles")
    var totalFiles:Int = 1,

    @ColumnInfo(name = "downloaded")
    var downloaded:Boolean=false,

    @ColumnInfo(name = "directory")
    var directory:String?=null
):Parcelable