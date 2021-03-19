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

package com.shabinder.common.models.spotify

import kotlinx.serialization.Serializable

@Serializable
data class Album(
    var album_type: String? = null,
    var artists: List<Artist?>? = null,
    var available_markets: List<String?>? = null,
    var copyrights: List<Copyright?>? = null,
    var external_ids: Map<String?, String?>? = null,
    var external_urls: Map<String?, String?>? = null,
    var genres: List<String?>? = null,
    var href: String? = null,
    var id: String? = null,
    var images: List<Image?>? = null,
    var label: String? = null,
    var name: String? = null,
    var popularity: Int? = null,
    var release_date: String? = null,
    var release_date_precision: String? = null,
    var tracks: PagingObjectTrack? = null,
    var type: String? = null,
    var uri: String? = null
)
