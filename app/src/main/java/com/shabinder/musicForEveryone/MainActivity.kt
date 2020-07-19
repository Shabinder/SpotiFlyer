package com.shabinder.musicForEveryone

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.musicForEveryone.databinding.MainActivityBinding
import com.shabinder.musicForEveryone.utils.YoutubeConnector
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import com.spotify.sdk.android.authentication.LoginActivity
import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.SpotifyService
import retrofit.RestAdapter


class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding
    var ytDownloader : YoutubeDownloader? = null
    val REDIRECT_URI = "musicforeveryone://callback"
    val CLIENT_ID:String = "694d8bf4f6ec420fa66ea7fb4c68f89d"
//    val musicDirectory = File(this.filesDir?.absolutePath + "/Music/")
    var message :String =""
    var token :String =""
    var spotify: SpotifyService? = null
    lateinit var sharedViewModel: SharedViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.main_activity)

        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        //TODO Use Coroutines
        if(spotify==null){
            authenticateSpotify()
        }
        requestPermission()

        //Object to download From Youtube {"https://github.com/sealedtx/java-youtube-downloader"}
        ytDownloader = YoutubeDownloader()
        sharedViewModel.ytDownloader = ytDownloader
        //Initialing Communication with Youtube
        YoutubeConnector.youtubeConnector()



    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, intent)
        // Check if result comes from the correct activity
        if (requestCode == LoginActivity.REQUEST_CODE) {
            val response = AuthenticationClient.getResponse(resultCode, intent)
            when (response.type) {
                AuthenticationResponse.Type.TOKEN -> {
                    Log.i("Network",response.accessToken.toString())
                    token = response.accessToken
                    sharedViewModel.accessToken = response.accessToken

                    //Implementing My Own Spotify Requests
                    implementSpotifyExtra()
                    val api = SpotifyApi()
                    api.setAccessToken(token)
                    spotify = api.service
                    sharedViewModel.spotify = api.service
                    //Initiate Processes In Main Fragment
                    val me = spotify?.me?.display_name
                    sharedViewModel.userName.value = me
                    Log.i("Network",me!!)
                }
                AuthenticationResponse.Type.ERROR -> {
                    Log.i("Network",response.error.toString())

                }
                else -> {
                }
            }
        }
    }

    /**
     * Adding my own new Spotify Web Api Requests!
     * */
    private fun implementSpotifyExtra() {
        val restAdapter = RestAdapter.Builder()
            .setEndpoint(SpotifyApi.SPOTIFY_WEB_API_ENDPOINT)
            .setRequestInterceptor { request ->
                request.addHeader(
                    "Authorization",
                    "Bearer $token"
                )
            }
            .build()

        val spotifyExtra = restAdapter.create(SpotifyNewService::class.java)
        sharedViewModel.spotifyExtra = spotifyExtra
    }


    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                786
            )
        }
    }

    private fun authenticateSpotify() {
        val builder =  AuthenticationRequest.Builder(CLIENT_ID,AuthenticationResponse.Type.TOKEN,REDIRECT_URI)
            .setShowDialog(true)
            .setScopes(arrayOf("user-read-private","streaming","user-read-email","user-modify-playback-state","user-top-read","user-library-modify","user-read-currently-playing","user-library-read","user-read-recently-played"))
        val request: AuthenticationRequest = builder.build()
        AuthenticationClient.openLoginActivity(this, LoginActivity.REQUEST_CODE, request)
    }


}