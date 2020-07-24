package com.shabinder.musicForEveryone

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.musicForEveryone.databinding.MainActivityBinding
import com.shabinder.musicForEveryone.downloadHelper.DownloadHelper
import com.shabinder.musicForEveryone.utils.SpotifyService
import com.shabinder.musicForEveryone.utils.SpotifyServiceToken
import com.shabinder.musicForEveryone.utils.YoutubeInterface
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() ,DownloadHelper{
    private lateinit var binding: MainActivityBinding
    private var ytDownloader : YoutubeDownloader? = null
    private var spotifyService : SpotifyService? = null
    private var spotifyServiceToken : SpotifyServiceToken? = null
    private var downloadManager : DownloadManager? = null
//    private val redirectUri = "musicforeveryone://callback"
    private val clientId:String = "694d8bf4f6ec420fa66ea7fb4c68f89d"
    private val clientSecret:String = "02ca2d4021a7452dae2328b47a6e8fe8"
    private var isConnected: Boolean = false
    private var sharedPref :SharedPreferences? = null

    private var token :String =""
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.main_activity)
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        sharedPref = this.getPreferences(Context.MODE_PRIVATE)

//        if(sharedPref?.contains("token")!! && (sharedPref?.getLong("time",System.currentTimeMillis()/1000/60/60)!! < (System.currentTimeMillis()/1000/60/60)) ){
//            val savedToken = sharedPref?.getString("token","error")!!
//            sharedViewModel.accessToken.value = savedToken
//            Log.i("SharedPrefs Token:",savedToken)
//            token = savedToken
//
//            implementSpotifyService(savedToken)
//        }else{authenticateSpotify()}

        if(sharedViewModel.spotifyService == null){
            authenticateSpotify()
        }else{
            implementSpotifyService(sharedViewModel.accessToken.value!!)
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

        isConnected = isOnline()
        sharedViewModel.isConnected.value = isConnected

        Log.i("Connection Status",isConnected.toString())


        handleIntentFromExternalActivity()
    }

    /**
     * Adding my own new Spotify Web Api Requests!
     * */
    private fun implementSpotifyService(token:String) {
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

        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.spotify.com/v1/")
                .client(httpClient.build())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

        spotifyService = retrofit.create(SpotifyService::class.java)
        sharedViewModel.spotifyService = spotifyService
    }

    private fun getSpotifyToken(){
        val httpClient2: OkHttpClient.Builder = OkHttpClient.Builder()
        httpClient2.addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request: Request =
                    chain.request().newBuilder().addHeader(
                        "Authorization",
                        "Basic ${android.util.Base64.encodeToString("$clientId:$clientSecret".toByteArray(),android.util.Base64.NO_WRAP)}"
                    ).build()
                return chain.proceed(request)
            }
        })

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit2 = Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/")
            .client(httpClient2.build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        spotifyServiceToken = retrofit2.create(SpotifyServiceToken::class.java)

    }

    private fun authenticateSpotify() {
        if (spotifyServiceToken == null) {
            getSpotifyToken()
        }
        sharedViewModel.uiScope.launch {
            if (isConnected) {
                Log.i("Post Request", "Made")
                token = spotifyServiceToken!!.getToken()!!.access_token
                implementSpotifyService(token)
                Log.i("Post Request", token)
                sharedViewModel.accessToken.value = token
                saveToken(token)
            }else{
                Log.i("network", "unavailable")
//                sharedViewModel.showAlertDialog(resources,this@MainActivity)
            }
        }
    }

    private fun saveToken(token:String) {
        with (sharedPref?.edit()) {
            this?.let {
                putString("token", token)
                putLong("time",(System.currentTimeMillis()/1000/60/60))
                commit()
            }
        }
    }

    private fun handleIntentFromExternalActivity() {
        if (intent?.action == Intent.ACTION_SEND) {
            if ("text/plain" == intent.type) {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                    Log.i("Intent Received",it)
                    sharedViewModel.intentString = it
                }
            }
        }
    }

    private fun isOnline(): Boolean {
        val cm =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                786
            )
        }
    }

    override fun onSaveInstanceState(savedInstanceState:Bundle) {
        savedInstanceState.putString("token",token)
        super.onSaveInstanceState(savedInstanceState)
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        if (savedInstanceState.getString("token") ==""){
            super.onRestoreInstanceState(savedInstanceState)
        }else{
            implementSpotifyService(savedInstanceState.getString("token")!!)
            super.onRestoreInstanceState(savedInstanceState)
        }
    }

    /*
    private fun authenticateSpotify() {
        val builder =  AuthenticationRequest.Builder(clientId,AuthenticationResponse.Type.TOKEN,redirectUri)
            .setScopes(arrayOf("user-read-private"))
//            .setScopes(arrayOf("user-read-private","streaming","user-read-email","user-modify-playback-state","user-top-read","user-library-modify","user-read-currently-playing","user-library-read","user-read-recently-played"))
        val request: AuthenticationRequest = builder.build()
        AuthenticationClient.openLoginActivity(this, LoginActivity.REQUEST_CODE, request)
    }*/

    /*override fun onActivityResult(
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
                  implementSpotifyService(token)

                  sharedViewModel.uiScope.launch {
                      val me = spotifyService?.getMe()?.display_name
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
                  Log.i("Network","Something Weird Happened While Authenticating")
              }
          }
      }
  }
*/


}