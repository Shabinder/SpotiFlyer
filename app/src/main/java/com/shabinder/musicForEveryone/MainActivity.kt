package com.shabinder.musicForEveryone

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.musicForEveryone.databinding.MainActivityBinding
import com.shabinder.musicForEveryone.downloadHelper.DownloadHelper
import com.shabinder.musicForEveryone.utils.SpotifyNewService
import com.shabinder.musicForEveryone.utils.YoutubeInterface
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import com.spotify.sdk.android.authentication.LoginActivity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.SpotifyService
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class MainActivity : AppCompatActivity() ,DownloadHelper{
    private lateinit var binding: MainActivityBinding
    var ytDownloader : YoutubeDownloader? = null
    var spotifyExtra : SpotifyNewService? = null
    var downloadManager : DownloadManager? = null
    val REDIRECT_URI = "musicforeveryone://callback"
    val CLIENT_ID:String = "694d8bf4f6ec420fa66ea7fb4c68f89d"
    var token :String =""
    var spotify: SpotifyService? = null
    lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.main_activity)

        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
//        val policy =
//            StrictMode.ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)
        //TODO Use Coroutines
        if(spotify==null){
            authenticateSpotify()
        }
        requestPermission()

        //Object to download From Youtube {"https://github.com/sealedtx/java-youtube-downloader"}
        ytDownloader = YoutubeDownloader()
        sharedViewModel.ytDownloader = ytDownloader
        //Initialing Communication with Youtube
        YoutubeInterface.youtubeConnector()

        //Getting System Download Manager
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        sharedViewModel.downloadManager = downloadManager

        if (intent?.action == Intent.ACTION_SEND) {
            if ("text/plain" == intent.type) {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                    Log.i("Intent Received",it)
                sharedViewModel.intentString = it
                }
            }
        }
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

                    sharedViewModel.uiScope.launch {
                        val me = spotifyExtra?.getMe()?.display_name
                        sharedViewModel.userName.value = "Logged in as: $me"
                        Log.i("Network","Hello, " + me!!)
                    }

                    sharedViewModel.userName.observe(this, Observer {
                        binding.message.text = it
                    })
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

        val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()

        httpClient.addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request: Request =
                    chain.request().newBuilder().addHeader(
                        "Authorization",
                        "Bearer $token"
                    ).build()
                return chain.proceed(request)

            }
        })

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit: Retrofit =
            Retrofit.Builder()
                .baseUrl("https://api.spotify.com/v1/")
                .client(httpClient.build())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

        spotifyExtra = retrofit.create(SpotifyNewService::class.java)
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
            .setShowDialog(false)
            .setScopes(arrayOf("user-read-private","streaming","user-read-email","user-modify-playback-state","user-top-read","user-library-modify","user-read-currently-playing","user-library-read","user-read-recently-played"))
        val request: AuthenticationRequest = builder.build()
        AuthenticationClient.openLoginActivity(this, LoginActivity.REQUEST_CODE, request)
    }


}