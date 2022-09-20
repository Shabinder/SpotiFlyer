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
data class artistAlbums(
    var href: String? = null,
    var items: List<Album?>? = null,
    var limit: Int? = null,
    var next: String? = null,
    var previous: String? = null,
    var offset: Int? = null,
    var total: Int? = null
)