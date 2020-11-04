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

package com.shabinder.spotiflyer.ui.downloadrecord

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.database.DownloadRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DownloadRecordViewModel @ViewModelInject constructor(val databaseDAO: DatabaseDAO) :
    ViewModel(){
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    var spotifyList = mutableListOf<DownloadRecord>()
    var ytList = mutableListOf<DownloadRecord>()
    val downloadRecordList = MutableLiveData<MutableList<DownloadRecord>>().apply {
        value = mutableListOf()
    }
    init {
        getDownloadRecordList()
    }
    private fun getDownloadRecordList() {
        uiScope.launch {
            downloadRecordList.postValue(databaseDAO.getRecord().toMutableList())
        }
    }
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}