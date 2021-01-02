package com.shabinder.spotiflyer.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@EntryPoint
@InstallIn(SingletonComponent::class)
interface Directories {
    @DefaultDir fun defaultDir():String
    @ImageDir fun imageDir():String
}