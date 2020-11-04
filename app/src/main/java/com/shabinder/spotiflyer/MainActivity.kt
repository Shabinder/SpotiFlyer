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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.shabinder.spotiflyer.databinding.MainActivityBinding
import com.shabinder.spotiflyer.downloadHelper.SpotifyDownloadHelper
import com.shabinder.spotiflyer.utils.SpotifyService
import com.shabinder.spotiflyer.utils.SpotifyServiceTokenRequest
import com.shabinder.spotiflyer.utils.createDirectories
import com.squareup.moshi.Moshi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainActivity : AppCompatActivity(){
    private var spotifyService : SpotifyService? = null
    private var isConnected: Boolean = false
    private var sharedPref :SharedPreferences? = null
    private var token :String =""
    private lateinit var binding: MainActivityBinding
    private lateinit var sharedViewModel: SharedViewModel
    @Inject lateinit var spotifyServiceTokenRequest: SpotifyServiceTokenRequest
    @Inject lateinit var moshi: Moshi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        //Enabling Dark Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        sharedPref = this.getPreferences(Context.MODE_PRIVATE)

        //starting Notification and Downloader Service!
        SpotifyDownloadHelper.startService(this)

        if(sharedViewModel.spotifyService.value == null){
            authenticateSpotify()
        }else{
            implementSpotifyService(sharedViewModel.accessToken.value!!)
        }

        requestPermission()
        disableDozeMode()
        checkIfLatestVersion()
        createDirectories()
        isConnected = sharedViewModel.isOnline(this)
        sharedViewModel.isConnected.value = isConnected
        Log.i("Connection Status", isConnected.toString())

        handleIntentFromExternalActivity()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.i("NEW INTENT", "Received")
        handleIntentFromExternalActivity(intent)
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
    private fun implementSpotifyService(token: String) {
        val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
        httpClient.addInterceptor(Interceptor { chain ->
            val request: Request =
                chain.request().newBuilder().addHeader(
                    "Authorization",
                    "Bearer $token"
                ).build()
            chain.proceed(request)
        })

        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.spotify.com/v1/")
                .client(httpClient.build())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

        spotifyService = retrofit.create(SpotifyService::class.java)
        sharedViewModel.spotifyService.value = spotifyService
    }


    fun authenticateSpotify() {
        sharedViewModel.uiScope.launch {
            if (isConnected) {
                Log.i("Post Request", "Made")
                token = spotifyServiceTokenRequest.getToken()!!.access_token
                implementSpotifyService(token)
                Log.i("Post Request", token)
                sharedViewModel.accessToken.value = token
            }else{
                Log.i("network", "unavailable")
//                sharedViewModel.showAlertDialog(resources,this@MainActivity)
            }
        }
    }


    private fun handleIntentFromExternalActivity(intent: Intent? = getIntent()) {
        if (intent?.action == Intent.ACTION_SEND) {
            if ("text/plain" == intent.type) {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                    Log.i("Intent Received", it)
                    sharedViewModel.intentString.value = it
                }
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                786
            )
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString("token", token)
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

    companion object{
        private var instance = MainActivity()
        fun getInstance():MainActivity{
            return instance
        }
    }
    init {
        instance = this
    }
}