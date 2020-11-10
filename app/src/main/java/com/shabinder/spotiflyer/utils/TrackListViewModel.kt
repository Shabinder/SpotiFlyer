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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shabinder.spotiflyer.models.TrackDetails
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

abstract class TrackListViewModel:ViewModel() {
    abstract var folderType:String
    abstract var subFolder:String
    open val trackList = MutableLiveData<MutableList<TrackDetails>>()

    private val viewModelJob:CompletableJob = Job()
    open val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val loading = "Loading!"
    open var title = MutableLiveData<String>().apply { value = loading }
    open var coverUrl = MutableLiveData<String>().apply { value = loading }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}