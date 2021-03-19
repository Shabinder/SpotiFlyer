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
data class Episodes(
    var audio_preview_url: String?,
    var description: String?,
    var duration_ms: Int?,
    var explicit: Boolean?,
    var external_urls: Map<String, String>?,
    var href: String?,
    var id: String?,
    var images: List<Image?>?,
    var is_externally_hosted: Boolean?,
    var is_playable: Boolean?,
    var language: String?,
    var languages: List<String?>?,
    var name: String?,
    var release_date: String?,
    var release_date_precision: String?,
    var type: String?,
    var uri: String
)
