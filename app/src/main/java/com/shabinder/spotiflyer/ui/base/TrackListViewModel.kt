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

package com.shabinder.spotiflyer.ui.base

import androidx.lifecycle.ViewModel
import com.shabinder.spotiflyer.models.TrackDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class TrackListViewModel:ViewModel() {
    abstract var folderType:String
    abstract var subFolder:String
    private val _trackList = MutableStateFlow<List<TrackDetails>>(mutableListOf())
    open val trackList:StateFlow<List<TrackDetails>>
        get() = _trackList

    fun updateTrackList(list:List<TrackDetails>){
        _trackList.value = list
    }

    private val loading = "Loading!"
    open var title = MutableStateFlow(loading)
    open var coverUrl = MutableStateFlow(loading)

}