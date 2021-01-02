package com.shabinder.spotiflyer.di

import android.content.Context
import android.os.Environment
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.database.DownloadRecordDatabase
import com.shabinder.spotiflyer.networking.SpotifyServiceTokenRequest
import com.shabinder.spotiflyer.utils.TokenStore
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
    @ImageDir fun imageDir(@ApplicationContext appContext: Context):String =
        appContext.cacheDir.absolutePath + File.separator

    @Provides
    @Singleton
    @Suppress("DEPRECATION")
    //Default Directory to save Media in their Own Categorized Folders
    @DefaultDir fun defaultDir(@ApplicationContext appContext: Context):String =
        appContext.externalMediaDirs[0].absolutePath +
                File.separator

    @Provides
    @Singleton
    fun databaseDAO(@ApplicationContext appContext: Context): DatabaseDAO {
        return DownloadRecordDatabase.getInstance(appContext).databaseDAO
    }

    @Provides
    @Singleton
    fun provideDirectories(@ApplicationContext appContext: Context):Directories = EntryPoints.get(appContext, Directories::class.java)

    @Provides
    @Singleton
    fun getTokenStore(
        @ApplicationContext appContext: Context,
        spotifyServiceTokenRequest: SpotifyServiceTokenRequest
    ): TokenStore = TokenStore(appContext,spotifyServiceTokenRequest)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDir

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ImageDir

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FinalOutputDir