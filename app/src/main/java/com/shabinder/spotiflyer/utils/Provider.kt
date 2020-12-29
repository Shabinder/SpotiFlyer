package com.shabinder.spotiflyer.utils

import android.content.Context
import android.os.Environment
import android.util.Base64
import androidx.lifecycle.ViewModelProvider
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.spotiflyer.App
import com.shabinder.spotiflyer.SharedViewModel
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.database.DownloadRecordDatabase
import com.shabinder.spotiflyer.networking.GaanaInterface
import com.shabinder.spotiflyer.networking.SpotifyServiceTokenRequest
import com.shabinder.spotiflyer.networking.YoutubeMusicApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object Provider {

    //Default Directory to save Media in their Own Categorized Folders
    @Suppress("DEPRECATION")// We Do Have Media Access (But Just Media in Media Directory,Not Anything Else)
    val defaultDir = Environment.getExternalStorageDirectory().toString() + File.separator +
            Environment.DIRECTORY_MUSIC + File.separator +
            "SpotiFlyer"+ File.separator

    //Default Cache Directory to save Album Art to use them for writing in Media Later
    fun imageDir(ctx: Context = mainActivity): String = ctx
        .externalCacheDir?.absolutePath + File.separator +
            ".Images" + File.separator


    @Provides
    @Singleton
    fun databaseDAO(@ApplicationContext appContext: Context): DatabaseDAO {
        return DownloadRecordDatabase.getInstance(appContext).databaseDAO
    }

    @Provides
    @Singleton
    fun getYTDownloader(): YoutubeDownloader {
        return YoutubeDownloader()
    }

    @Provides
    @Singleton
    fun getMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun getSpotifyTokenInterface(moshi: Moshi): SpotifyServiceTokenRequest {
        val httpClient2: OkHttpClient.Builder = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val request: Request =
                    chain.request().newBuilder()
                        .addHeader(
                            "Authorization",
                            "Basic ${
                                Base64.encodeToString(
                                    "${App.clientId}:${App.clientSecret}".toByteArray(),
                                    Base64.NO_WRAP
                                )
                            }"
                        ).build()
                chain.proceed(request)
            }).addInterceptor(NetworkInterceptor())

        val retrofit = Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/")
            .client(httpClient2.build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create(SpotifyServiceTokenRequest::class.java)
    }

    @Provides
    @Singleton
    fun okHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(NetworkInterceptor())
            .build()
    }

    @Provides
    @Singleton
    fun getGaanaInterface(moshi: Moshi, okHttpClient: OkHttpClient): GaanaInterface {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.gaana.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create(GaanaInterface::class.java)
    }

    @Provides
    @Singleton
    fun getYoutubeMusicApi(moshi: Moshi): YoutubeMusicApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://music.youtube.com/youtubei/v1/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create(YoutubeMusicApi::class.java)
    }

}