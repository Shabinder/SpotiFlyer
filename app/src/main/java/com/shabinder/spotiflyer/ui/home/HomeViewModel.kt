package com.shabinder.spotiflyer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.database.DownloadRecord
import com.shabinder.spotiflyer.utils.sharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    private val _link = MutableStateFlow("")
    val link:StateFlow<String>
        get() = _link

    fun updateLink(s:String) {
        _link.value = s
    }

    private val _isAuthenticating = MutableStateFlow(true)
    val isAuthenticating:StateFlow<Boolean>
        get() = _isAuthenticating

    fun authenticated(s:Boolean) {
        _isAuthenticating.value = s
    }

    private val _selectedCategory = MutableStateFlow(HomeCategory.About)
    val selectedCategory :StateFlow<HomeCategory>
        get() = _selectedCategory

    fun selectCategory(s:HomeCategory) {
        _selectedCategory.value = s
    }

    private val _downloadRecordList = MutableStateFlow<List<DownloadRecord>>(listOf())
    val downloadRecordList: StateFlow<List<DownloadRecord>>
        get() = _downloadRecordList

    fun getDownloadRecordList() {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                _downloadRecordList.value = sharedViewModel.databaseDAO.getRecord().toMutableList()
            }
        }
    }

    init {
        getDownloadRecordList()
    }
}

enum class HomeCategory {
    About, History
}