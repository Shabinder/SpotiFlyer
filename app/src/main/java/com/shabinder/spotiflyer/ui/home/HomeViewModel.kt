package com.shabinder.spotiflyer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shabinder.spotiflyer.database.DownloadRecord
import com.shabinder.spotiflyer.utils.sharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class HomeViewModel : ViewModel() {

    var link by mutableStateOf("")
    private set

    fun updateLink(s:String) {
        link = s
    }

    var selectedCategory by mutableStateOf(HomeCategory.About)
    private set

    fun selectCategory(s:HomeCategory) {
        selectedCategory = s
    }

    var downloadRecordList by mutableStateOf<List<DownloadRecord>>(listOf())

    fun getDownloadRecordList() {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                downloadRecordList = sharedViewModel.databaseDAO.getRecord()
            }
        }
    }
}

enum class HomeCategory {
    About, History
}