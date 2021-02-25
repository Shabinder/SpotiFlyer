package com.shabinder.common.di

sealed class NetworkResponse<out T> {
    data class Success<T>(val value:T):NetworkResponse<T>()
    data class Error(val message:String):NetworkResponse<Nothing>()
}