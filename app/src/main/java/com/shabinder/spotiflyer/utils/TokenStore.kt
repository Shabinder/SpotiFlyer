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

package com.shabinder.spotiflyer.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import com.shabinder.spotiflyer.models.spotify.Token
import com.shabinder.spotiflyer.networking.SpotifyServiceTokenRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor (
    @ApplicationContext context: Context,
    private val spotifyServiceTokenRequest: SpotifyServiceTokenRequest
) {
    private val dataStore = context.createDataStore(
        name = "settings"
    )
    private val token = preferencesKey<String>("token")
    private val tokenExpiry = preferencesKey<Long>("expiry")


    suspend fun saveToken(tokenKey:String,time:Long){
        dataStore.edit {
            it[token] = tokenKey
            it[tokenExpiry] = (System.currentTimeMillis()/1000) + time
        }
    }

    suspend fun getToken(): Token?{
        var token = dataStore.data.map {
            Token(it[token],null,it[tokenExpiry])
        }.firstOrNull()
        if(System.currentTimeMillis()/1000 > token?.expiry?:0){
            token = spotifyServiceTokenRequest.getToken().value
            log("Spotify Token","Requesting New Token")
            GlobalScope.launch { token?.access_token?.let { saveToken(it,token.expiry ?: 0) } }
        }
        return token
    }

}