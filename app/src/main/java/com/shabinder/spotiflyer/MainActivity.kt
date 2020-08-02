/*
 * Copyright (C)  2020  Shabinder Singh
 *
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.spotiflyer.databinding.MainActivityBinding
import com.shabinder.spotiflyer.downloadHelper.DownloadHelper
import com.shabinder.spotiflyer.utils.SpotifyService
import com.shabinder.spotiflyer.utils.SpotifyServiceToken
import com.shabinder.spotiflyer.utils.createDirectory
import com.shreyaspatil.EasyUpiPayment.EasyUpiPayment
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
class MainActivity : AppCompatActivity(){
    private lateinit var binding: MainActivityBinding
    private var ytDownloader : YoutubeDownloader? = null
    private var spotifyService : SpotifyService? = null
    private var spotifyServiceToken : SpotifyServiceToken? = null
//    private val redirectUri = "spotiflyer://callback"
    private val clientId:String = "694d8bf4f6ec420fa66ea7fb4c68f89d"
    private val clientSecret:String = "02ca2d4021a7452dae2328b47a6e8fe8"
    private var isConnected: Boolean = false
    private var sharedPref :SharedPreferences? = null
    private var easyUpiPayment:EasyUpiPayment? = null
    private var token :String =""
    private lateinit var sharedViewModel: SharedViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.main_activity)
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        //starting Notification and Downloader Service!
        DownloadHelper.startService(this)

/*        if(sharedPref?.contains("token")!! && (sharedPref?.getLong("time",System.currentTimeMillis()/1000/60/60)!! < (System.currentTimeMillis()/1000/60/60)) ){
            val savedToken = sharedPref?.getString("token","error")!!
            sharedViewModel.accessToken.value = savedToken
            Log.i("SharedPrefs Token:",savedToken)
            token = savedToken

            implementSpotifyService(savedToken)
        }else{authenticateSpotify()}*/

        if(sharedViewModel.spotifyService == null){
            authenticateSpotify()
        }else{
            implementSpotifyService(sharedViewModel.accessToken.value!!)
        }

        requestPermission()
        disableDozeMode()
        checkIfLatestVersion()
        createDir()
        setUpi()
        isConnected = isOnline()
        sharedViewModel.isConnected.value = isConnected
        Log.i("Connection Status",isConnected.toString())

        //Object to download From Youtube {"https://github.com/sealedtx/java-youtube-downloader"}
        ytDownloader = YoutubeDownloader()
        sharedViewModel.ytDownloader = ytDownloader

        handleIntentFromExternalActivity()
    }

    @SuppressLint("BatteryLife")
    fun disableDozeMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm =
                this.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(packageName)
            if (!isIgnoringBatteryOptimizations) {
                val intent = Intent()
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, 1233)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1233) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val pm =
                    getSystemService(Context.POWER_SERVICE) as PowerManager
                val isIgnoringBatteryOptimizations =
                    pm.isIgnoringBatteryOptimizations(packageName)
                if (isIgnoringBatteryOptimizations) {
                    // Ignoring battery optimization
                } else {
                    disableDozeMode()//Again Ask For Permission!!
                }
            }
        }
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

    fun authenticateSpotify() {
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

    private fun setUpi() {
        easyUpiPayment = EasyUpiPayment.Builder()
            .with(this)
            .setPayeeVpa("technoshab@paytm")
            .setPayeeName("Shabinder Singh")
            .setTransactionId("UNIQUE_TRANSACTION_ID")
            .setTransactionRefId("UNIQUE_TRANSACTION_REF_ID")
            .setDescription("Thanks for donating")
            .setAmount("39.00")
            .build()

        sharedViewModel.easyUpiPayment = easyUpiPayment

    }

    private fun createDir() {
        createDirectory(DownloadHelper.defaultDir)
        createDirectory(DownloadHelper.defaultDir+".Images/")
        createDirectory(DownloadHelper.defaultDir+"Tracks/")
        createDirectory(DownloadHelper.defaultDir+"Albums/")
        createDirectory(DownloadHelper.defaultDir+"Playlists/")
        createDirectory(DownloadHelper.defaultDir+"YT_Downloads/")
    }

    private fun checkIfLatestVersion() {
        val appUpdater = AppUpdater(this)
            .showAppUpdated(false)//true:Show App is Update Dialog
            .setUpdateFrom(UpdateFrom.XML)
            .setUpdateXML("https://raw.githubusercontent.com/Shabinder/SpotiFlyer/master/app/src/main/res/xml/app_update.xml")
            .setCancelable(false)
            .setButtonUpdateClickListener { _, _ ->
                val uri: Uri =
                    Uri.parse("http://github.com/Shabinder/SpotiFlyer/releases")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            .setButtonDismissClickListener { dialog, _ ->
                dialog.dismiss()
            }
        appUpdater.start()
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