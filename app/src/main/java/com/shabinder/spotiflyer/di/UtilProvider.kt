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

package com.shabinder.spotiflyer.di

import android.content.Context
import android.os.Environment
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.database.DownloadRecordDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object UtilProvider {

    //Default Cache Directory to save Album Art to use them for writing in Media Later
    //Gets Cleaned After Service Destroy
    @Provides
    @Singleton
    @ImageDir fun provideImageDir(@ApplicationContext appContext: Context):String =
        appContext.cacheDir.absolutePath + File.separator

    @Provides
    @Singleton
    @Suppress("DEPRECATION")
    //Default Directory to save Media in their Own Categorized Folders
    @DefaultDir fun provideDefaultDir(@ApplicationContext appContext: Context):String =
        Environment.getExternalStorageDirectory().toString() + File.separator +
                Environment.DIRECTORY_MUSIC + File.separator +
                "SpotiFlyer"+ File.separator

    @Provides
    @Singleton
    fun provideDatabaseDAO(@ApplicationContext appContext: Context): DatabaseDAO {
        return DownloadRecordDatabase.getInstance(appContext).databaseDAO
    }

    @Provides
    @Singleton
    fun provideDirectories(@ApplicationContext appContext: Context):Directories = EntryPoints.get(appContext, Directories::class.java)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDir

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ImageDir