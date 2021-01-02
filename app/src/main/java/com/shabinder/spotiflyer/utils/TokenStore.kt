package com.shabinder.spotiflyer.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import com.shabinder.spotiflyer.models.spotify.Token
import com.shabinder.spotiflyer.networking.SpotifyServiceTokenRequest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TokenStore (
    context: Context,
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