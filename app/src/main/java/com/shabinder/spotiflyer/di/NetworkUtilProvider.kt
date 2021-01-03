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

import android.util.Base64
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.spotiflyer.App
import com.shabinder.spotiflyer.networking.*
import com.shabinder.spotiflyer.utils.NetworkInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object NetworkUtilProvider {

    @Provides
    @Singleton
    fun provideYTDownloader(): YoutubeDownloader = YoutubeDownloader()

    @Provides
    @Singleton
    fun provideSpotifyApi(authInterceptor: SpotifyAuthInterceptor,okHttpClient: OkHttpClient.Builder,moshi: Moshi) :SpotifyApi =
        Retrofit.Builder().run{
            baseUrl("https://api.spotify.com/v1/")
            client(okHttpClient.addInterceptor(authInterceptor).build())
            addConverterFactory(MoshiConverterFactory.create(moshi))
            build()
        }.create(SpotifyApi::class.java)


    @Provides
    @Singleton
    fun provideSpotifyTokenInterface(moshi: Moshi,networkInterceptor: NetworkInterceptor): SpotifyServiceTokenRequest {
        val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
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
            }).addInterceptor(networkInterceptor)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/")
            .client(httpClient.build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create(SpotifyServiceTokenRequest::class.java)
    }

    @Provides
    @Singleton
    fun provideGaanaApi(moshi: Moshi, okHttpClient: OkHttpClient.Builder): GaanaApi
        = Retrofit.Builder().run {
            baseUrl("https://api.gaana.com/")
            client(okHttpClient.build())
            addConverterFactory(MoshiConverterFactory.create(moshi))
            build()
    }.create(GaanaApi::class.java)

    @Provides
    @Singleton
    fun provideYoutubeMusicApi(moshi: Moshi): YoutubeMusicApi
        = Retrofit.Builder().run {
            baseUrl("https://music.youtube.com/youtubei/v1/")
            addConverterFactory(ScalarsConverterFactory.create())
            addConverterFactory(MoshiConverterFactory.create(moshi))
            build()
        }.create(YoutubeMusicApi::class.java)

    @Provides
    @Singleton
    fun provideOkHttpClient(networkInterceptor: NetworkInterceptor): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .addInterceptor(networkInterceptor)

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
}