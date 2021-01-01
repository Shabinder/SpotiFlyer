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

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.models.PlatformQueryResult
import com.shabinder.spotiflyer.networking.GaanaInterface
import com.shabinder.spotiflyer.networking.SpotifyService
import com.shabinder.spotiflyer.ui.colorPrimary
import com.shabinder.spotiflyer.ui.colorPrimaryDark
import com.shabinder.spotiflyer.ui.home.HomeCategory
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@ActivityRetainedScoped
class SharedViewModel @ViewModelInject constructor(
    val databaseDAO: DatabaseDAO,
    val gaanaInterface : GaanaInterface,
    val ytDownloader: YoutubeDownloader
) : ViewModel() {
    var spotifyService = MutableStateFlow<SpotifyService?>(null)

    private val _gradientColor = MutableStateFlow(colorPrimaryDark)
    val gradientColor : StateFlow<Color>
        get() = _gradientColor

    fun updateGradientColor(color: Color) {
        _gradientColor.value = color
    }

    fun resetGradient() {
        _gradientColor.value = colorPrimaryDark
    }
}