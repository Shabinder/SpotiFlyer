package com.shabinder.spotiflyer.networking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shabinder.spotiflyer.models.spotify.Token
import com.shabinder.spotiflyer.utils.TokenStore
import com.shabinder.spotiflyer.utils.log
import com.shabinder.spotiflyer.utils.showDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor which adds authorization token in header.
 */
@Singleton
class SpotifyAuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {
    /*
    * Local Copy for Token
    * Live Throughout Session
    * */
    private var token by mutableStateOf<Token?>(null)

    override fun intercept(chain: Interceptor.Chain): Response {
        if(token?.expiry?:0 < System.currentTimeMillis()/1000){
            //Token Expired time to fetch New One
            runBlocking { token = tokenStore.getToken() }
            log("Spotify Auth",token.toString())
        }
        val authRequest = chain.request().newBuilder().
        addHeader("Authorization", "Bearer ${token?.access_token}").build()
        return chain.proceed(authRequest)
    }
}