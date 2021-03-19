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

package com.shabinder.common.models.wynk

// Use Kotlinx JSON Parsing as in YT Music
data class ShortURLWynk(
    val actualTotal: Int,
    val basicShortUrl: String,
    val branchUrl: String,
    val count: Int,
    val downloadUrl: String,
    val duration: Int,
    val exclusive: Boolean,
    val followCount: String,
    val id: String,
    val isCurated: Boolean,
    val isFollowable: Boolean,
    val isHt: Boolean,
    val itemIds: List<String>,
    val itemTypes: List<String>, // Songs , etc
    val items: List<ItemWynk>,
    val lang: String,
    val largeImage: String, // Cover Image Alternate
    val lastUpdated: Long,
    val offset: Int,
    val owner: String,
    val playIcon: Boolean,
    val playlistImage: String, // Cover Image
    val redesignFeaturedImage: String,
    val shortUrl: String,
    val singers: List<SingerWynk>,
    val smallImage: String,
    val title: String,
    val total: Int,
    val type: String
)
