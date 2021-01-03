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

package com.shabinder.spotiflyer

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.DpPropKey
import androidx.compose.animation.core.*
import androidx.compose.animation.transition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.shabinder.spotiflyer.models.DownloadStatus
import com.shabinder.spotiflyer.navigation.navigateToTrackList
import com.shabinder.spotiflyer.ui.ComposeLearnTheme
import com.shabinder.spotiflyer.ui.appNameStyle
import com.shabinder.spotiflyer.ui.colorOffWhite
import com.shabinder.spotiflyer.ui.home.*
import com.shabinder.spotiflyer.ui.splash.Splash
import com.shabinder.spotiflyer.utils.*
import com.tonyodev.fetch2.Status
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import kotlinx.coroutines.*

/*
* This is App's God Activity
* */
@AndroidEntryPoint
class MainActivity: AppCompatActivity(), PaymentResultListener {

    private lateinit var navController: NavHostController
    private lateinit var updateUIReceiver: BroadcastReceiver
    private lateinit var queryReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        // This app draws behind the system bars, so we want to handle fitting system windows
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ComposeLearnTheme {
                Providers(AmbientContentColor provides colorOffWhite) {
                    ProvideWindowInsets {

                        navController = rememberNavController()
                        var splashShown by remember { mutableStateOf(SplashState.Shown) }
                        val transition = transition(splashTransitionDefinition, splashShown)

                        Box{
                            Splash(
                                modifier = Modifier.alpha(transition[splashAlphaKey]),
                                onTimeout = { splashShown = SplashState.Completed }
                            )
                            MainScreen(
                                Modifier.alpha(transition[contentAlphaKey]),
                                this@MainActivity,
                                sharedViewModel,
                                navController,
                                transition[contentTopPaddingKey]
                            )
                        }
                    }
                }
            }
        }
        initialize()
    }

    private fun initialize() {
        Checkout.preload(applicationContext)
        requestStoragePermission()
        disableDozeMode()
        checkIfLatestVersion()
        createDirectories(
            sharedViewModel.directories.defaultDir(),
            sharedViewModel.directories.imageDir()
        )
        handleIntentFromExternalActivity()
    }

    private fun checkIfLatestVersion() {
        AppUpdater(this,0).run {
            setDisplay(Display.NOTIFICATION)
            showAppUpdated(false)//true:Show App is Updated Dialog
            setUpdateFrom(UpdateFrom.XML)
            setUpdateXML("https://raw.githubusercontent.com/Shabinder/SpotiFlyer/master/app/src/main/res/xml/app_update.xml")
            setCancelable(false)
            start()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntentFromExternalActivity(intent)
    }

    private fun initializeBroadcast(){
        val intentFilter = IntentFilter().apply {
            addAction(Status.QUEUED.name)
            addAction(Status.FAILED.name)
            addAction(Status.DOWNLOADING.name)
            addAction("Progress")
            addAction("Converting")
            addAction("track_download_completed")
        }
        updateUIReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                //UI update here
                if (intent != null) {
                    sharedViewModel.updateTrackStatus(intent)
                }
            }
        }
        val queryFilter = IntentFilter().apply { addAction("query_result") }
        queryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                //UI update here
                if (intent != null){
                    @Suppress("UNCHECKED_CAST")
                    val trackList = intent.getSerializableExtra("tracks") as HashMap<String, DownloadStatus>?
                    trackList?.let { list ->
                        log("Service Response", "${list.size} Tracks Active")
                        for (it in list) {
                            val position: Int = sharedViewModel.trackList.map { it.title }.indexOf(it.key)
                            log("BroadCast Received","$position, ${it.value} , ${it.key}")
                            sharedViewModel.updateTrackStatus(position,it.value)
                        }
                    }
                }
            }
        }
        registerReceiver(updateUIReceiver, intentFilter)
        registerReceiver(queryReceiver, queryFilter)
    }

    override fun onResume() {
        super.onResume()
        initializeBroadcast()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(updateUIReceiver)
        unregisterReceiver(queryReceiver)
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

    private fun handleIntentFromExternalActivity(intent: Intent? = getIntent()) {
        if (intent?.action == Intent.ACTION_SEND) {
            if ("text/plain" == intent.type) {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                    log("Intent Received", it)
                    GlobalScope.launch {
                        while(!this@MainActivity::navController.isInitialized){
                            //Wait for Navigation Controller to initialize
                            delay(200)
                        }
                        val filterLinkRegex = """http.+\w""".toRegex()
                        withContext(Dispatchers.Main) {
                            val string = it.replace("\n".toRegex(), " ")
                            val link = filterLinkRegex.find(string)?.value.toString()
                            log("Intent Link",link)
                            navController.navigateToTrackList(link)
                        }
                    }
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

    override fun onPaymentError(errorCode: Int, response: String?) {
        try{
            showDialog("Payment Failed", "$response")
        }catch (e: Exception){
            log("Razorpay Payment","Exception in onPaymentSuccess $response")
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        try{
            showDialog("Payment Successful", "ThankYou!")
        }catch (e: Exception){
            showDialog("Razorpay Payment, Error Occurred.")
            log("Razorpay Payment","Exception in onPaymentSuccess, ${e.message}")
        }
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
        /*actions = {
            Providers(AmbientContentAlpha provides ContentAlpha.medium) {
                IconButton(
                    onClick = { *//* TODO: Open Preferences*//* }
                ) {
                    Icon(Icons.Filled.Settings, tint = Color.Gray)
                }
            }
        },*/
        modifier = modifier,
        elevation = 0.dp
    )
}

enum class SplashState { Shown, Completed }

private val splashAlphaKey = FloatPropKey("Splash alpha")
private val contentAlphaKey = FloatPropKey("Content alpha")
private val contentTopPaddingKey = DpPropKey("Top padding")

private val splashTransitionDefinition = transitionDefinition<SplashState> {
    state(SplashState.Shown) {
        this[splashAlphaKey] = 1f
        this[contentAlphaKey] = 0f
        this[contentTopPaddingKey] = 100.dp
    }
    state(SplashState.Completed) {
        this[splashAlphaKey] = 0f
        this[contentAlphaKey] = 1f
        this[contentTopPaddingKey] = 0.dp
    }
    transition {
        splashAlphaKey using tween(
            durationMillis = 100
        )
        contentAlphaKey using tween(
            durationMillis = 300
        )
        contentTopPaddingKey using spring(
            stiffness = Spring.StiffnessLow
        )
    }
}

//@Preview(showBackground = true)
/*
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
}*/
