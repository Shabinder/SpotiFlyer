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

package com.shabinder.spotiflyer.utils

import com.shabinder.spotiflyer.downloadHelper.SpotifyDownloadHelper

fun createDirectories() {
    createDirectory(SpotifyDownloadHelper.defaultDir)
    createDirectory(SpotifyDownloadHelper.defaultDir + ".Images/")
    createDirectory(SpotifyDownloadHelper.defaultDir + "Tracks/")
    createDirectory(SpotifyDownloadHelper.defaultDir + "Albums/")
    createDirectory(SpotifyDownloadHelper.defaultDir + "Playlists/")
    createDirectory(SpotifyDownloadHelper.defaultDir + "YT_Downloads/")
}
fun getEmojiByUnicode(unicode: Int): String? {
    return String(Character.toChars(unicode))
}