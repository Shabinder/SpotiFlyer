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

package com.shabinder.spotiflyer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.networking.GaanaInterface
import com.shabinder.spotiflyer.networking.SpotifyService
import com.shabinder.spotiflyer.ui.colorPrimaryDark
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableStateFlow

@ActivityRetainedScoped
class SharedViewModel @ViewModelInject constructor(
    val databaseDAO: DatabaseDAO,
    val spotifyService: SpotifyService,
    val gaanaInterface : GaanaInterface,
    val ytDownloader: YoutubeDownloader
) : ViewModel() {
    var isAuthenticated by mutableStateOf(false)
        private set

    fun authenticated(s:Boolean) {
        isAuthenticated = s
    }

    var gradientColor by mutableStateOf(colorPrimaryDark)
    private set

    fun updateGradientColor(color: Color) {
        gradientColor = color
    }

    fun resetGradient() {
        gradientColor = colorPrimaryDark
    }
}