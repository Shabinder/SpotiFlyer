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

package com.shabinder.spotiflyer.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shabinder.spotiflyer.database.DownloadRecord
import com.shabinder.spotiflyer.utils.sharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    var selectedCategory by mutableStateOf(HomeCategory.About)
    private set

    fun selectCategory(s:HomeCategory) {
        selectedCategory = s
    }

    var downloadRecordList by mutableStateOf<List<DownloadRecord>>(listOf())

    fun getDownloadRecordList() {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                delay(100) //TEMP
                downloadRecordList = sharedViewModel.databaseDAO.getRecord()
            }
        }
    }
}

enum class HomeCategory {
    About, History
}