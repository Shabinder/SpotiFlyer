package com.shabinder.spotiflyer.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel: ViewModel() {

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

}

enum class HomeCategory {
    About, History
}