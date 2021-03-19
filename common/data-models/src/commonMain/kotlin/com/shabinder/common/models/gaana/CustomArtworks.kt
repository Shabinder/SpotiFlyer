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

package com.shabinder.common.models.gaana

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomArtworks(
    @SerialName("40x40") val size_40p: String,
    @SerialName("80x80") val size_80p: String,
    @SerialName("110x110")val size_110p: String,
    @SerialName("175x175")val size_175p: String,
    @SerialName("480x480")val size_480p: String,
)
