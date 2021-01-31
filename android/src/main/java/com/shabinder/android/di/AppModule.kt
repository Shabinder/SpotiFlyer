package com.shabinder.android.di

import com.shabinder.android.SharedViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { SharedViewModel(get(),get(),get(),get(),get()) }
}
