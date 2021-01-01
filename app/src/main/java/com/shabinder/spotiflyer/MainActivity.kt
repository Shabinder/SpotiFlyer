package com.shabinder.spotiflyer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.jetcaster.util.verticalGradientScrim
import com.shabinder.spotiflyer.navigation.ComposeNavigation
import com.shabinder.spotiflyer.navigation.navigateToPlatform
import com.shabinder.spotiflyer.networking.SpotifyService
import com.shabinder.spotiflyer.networking.SpotifyServiceTokenRequest
import com.shabinder.spotiflyer.ui.ComposeLearnTheme
import com.shabinder.spotiflyer.ui.appNameStyle
import com.shabinder.spotiflyer.ui.colorOffWhite
import com.shabinder.spotiflyer.utils.*
import com.squareup.moshi.Moshi
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import dev.chrisbanes.accompanist.insets.statusBarsHeight
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject
import com.shabinder.spotiflyer.utils.showDialog as showDialog1

/*
* This is App's God Activity
* */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavHostController
    @Inject lateinit var moshi: Moshi
    @Inject lateinit var spotifyServiceTokenRequest: SpotifyServiceTokenRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        // This app draws behind the system bars, so we want to handle fitting system windows
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ComposeLearnTheme {
                Providers(AmbientContentColor provides colorOffWhite) {
                    ProvideWindowInsets {
                        val appBarColor = MaterialTheme.colors.surface.copy(alpha = 0.6f)
                        navController = rememberNavController()

                        val gradientColor by sharedViewModel.gradientColor.collectAsState()

                        Column(
                            modifier = Modifier.fillMaxSize().verticalGradientScrim(
                                color = gradientColor.copy(alpha = 0.38f),
                                startYPercentage = 1f,
                                endYPercentage = 0f,
                                fixedHeight = 700f,
                            )
                        ) {
                            // Draw a scrim over the status bar which matches the app bar
                            Spacer(
                                Modifier.background(appBarColor).fillMaxWidth()
                                    .statusBarsHeight()
                            )
                            AppBar(
                                backgroundColor = appBarColor,
                                modifier = Modifier.fillMaxWidth()
                            )
                            ComposeNavigation(navController)
                        }
                    }
                }
            }
        }
        initialize()
    }

    private fun initialize() {
        authenticateSpotify()
        requestStoragePermission()
        disableDozeMode()
        //checkIfLatestVersion()
        createDirectories()
        handleIntentFromExternalActivity()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntentFromExternalActivity(intent)
    }

    @SuppressLint("BatteryLife")
    fun disableDozeMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm =
                this.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(packageName)
            if (!isIgnoringBatteryOptimizations) {
                val intent = Intent().apply{
                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    data = Uri.parse("package:$packageName")
                }
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
     * Adding my own Spotify Web Api Requests!
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
        }).addInterceptor(NetworkInterceptor())

        val retrofit = Retrofit.Builder().run{
            baseUrl("https://api.spotify.com/v1/")
            client(httpClient.build())
            addConverterFactory(MoshiConverterFactory.create(moshi))
            build()
        }
        sharedViewModel.spotifyService.value = retrofit.create(SpotifyService::class.java)
    }

    fun authenticateSpotify() {
        if(sharedViewModel.spotifyService.value == null){
            sharedViewModel.viewModelScope.launch {
                log("Spotify Authentication","Started")
                val token = spotifyServiceTokenRequest.getToken()
                token.value?.let {
                    showDialog1("Success: Spotify Token Acquired")
                    implementSpotifyService(it.access_token)
                }
                log("Spotify Token", token.value.toString())
            }
        }
    }


    private fun handleIntentFromExternalActivity(intent: Intent? = getIntent()) {
        if (intent?.action == Intent.ACTION_SEND) {
            if ("text/plain" == intent.type) {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                    log("Intent Received", it)
                    navController.navigateToPlatform(it)
                }
            }
        }
    }

    companion object{
        private lateinit var instance: MainActivity
        private lateinit var sharedViewModel: SharedViewModel
        fun getInstance():MainActivity = this.instance
        fun getSharedViewModel():SharedViewModel = this.sharedViewModel
    }

    init {
        instance = this
    }
}

@Composable
fun AppBar(
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        backgroundColor = backgroundColor,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    imageVector = vectorResource(R.drawable.ic_launcher_foreground)
                )
                Text(
                    text = "SpotiFlyer",
                    style = appNameStyle
                )
            }
        },
        actions = {
            Providers(AmbientContentAlpha provides ContentAlpha.medium) {
                IconButton(
                    onClick = { /* TODO: Open Preferences*/ }
                ) {
                    Icon(Icons.Filled.Settings, tint = Color.Gray)
                }
            }
        },
        modifier = modifier,
        elevation = 0.dp
    )
}


//@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeLearnTheme {
        ProvideWindowInsets {
            Column {
                val appBarColor = MaterialTheme.colors.surface.copy(alpha = 0.87f)

                // Draw a scrim over the status bar which matches the app bar
                Spacer(Modifier.background(appBarColor).fillMaxWidth().statusBarsHeight())

                AppBar(
                    backgroundColor = appBarColor,
                    modifier = Modifier.fillMaxWidth()
                )

                //ComposeNavigation()
            }
        }
    }
}