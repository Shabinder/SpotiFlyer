/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.rememberRootComponent
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.codekidlabs.storagechooser.R
import com.codekidlabs.storagechooser.StorageChooser
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.insets.statusBarsPadding
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.shabinder.common.di.*
import com.shabinder.common.di.worker.ForegroundService
import com.shabinder.common.models.Actions
import com.shabinder.common.models.AllPlatforms
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.PlatformActions
import com.shabinder.common.models.PlatformActions.Companion.SharedPreferencesKey
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.callbacks.SpotiFlyerRootCallBacks
import com.shabinder.common.uikit.*
import com.shabinder.spotiflyer.utils.*
import com.shabinder.common.models.Status
import com.shabinder.common.models.methods
import com.shabinder.spotiflyer.ui.NetworkDialog
import com.shabinder.spotiflyer.ui.PermissionDialog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.io.File

const val disableDozeCode = 1223

@ExperimentalAnimationApi
class MainActivity : ComponentActivity(), PaymentResultListener {

    private val fetcher: FetchPlatformQueryResult by inject()
    private val dir: Dir by inject()
    private lateinit var root: SpotiFlyerRoot
    private val callBacks: SpotiFlyerRootCallBacks
        get() = root.callBacks
    private val trackStatusFlow = MutableSharedFlow<HashMap<String, DownloadStatus>>(1)
    private var permissionGranted = mutableStateOf(true)
    private lateinit var updateUIReceiver: BroadcastReceiver
    private lateinit var queryReceiver: BroadcastReceiver
    private val internetAvailability by lazy { ConnectionLiveData(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This app draws behind the system bars, so we want to handle fitting system windows
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SpotiFlyerTheme {
                Surface(contentColor = colorOffWhite) {
                    ProvideWindowInsets {
                        permissionGranted = remember { mutableStateOf(true) }
                        val view = LocalView.current

                        Box {
                            root = SpotiFlyerRootContent(
                                rememberRootComponent(::spotiFlyerRoot),
                                Modifier.statusBarsPadding().navigationBarsPadding()
                            )
                            Spacer(
                                Modifier
                                    .statusBarsHeight()
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colors.background.copy(alpha = 0.65f))
                            )
                        }

                        LaunchedEffect(view) {
                            permissionGranted.value = checkPermissions()
                        }
                        NetworkDialog(isInternetAvailableState())
                        PermissionDialog(
                            permissionGranted.value,
                            { requestStoragePermission() },
                            { disableDozeMode(disableDozeCode) }
                        )
                    }
                }
            }
        }
        initialise()
    }

    private fun initialise() {
        checkIfLatestVersion()
        Checkout.preload(applicationContext)
        handleIntentFromExternalActivity()
    }

    @Composable
    private fun isInternetAvailableState(): State<Boolean?> {
        return internetAvailability.observeAsState()
    }

    @Suppress("DEPRECATION")
    private fun setUpOnPrefClickListener() {
        // Initialize Builder
        val chooser = StorageChooser.Builder()
            .withActivity(this)
            .withFragmentManager(fragmentManager)
            .withMemoryBar(true)
            .setTheme(StorageChooser.Theme(applicationContext).apply {
                scheme = applicationContext.resources.getIntArray(R.array.default_dark)
            })
            .setDialogTitle("Set Download Directory")
            .allowCustomPath(true)
            .setType(StorageChooser.DIRECTORY_CHOOSER)
            .build()

        // get path that the user has chosen
        chooser.setOnSelectListener { path ->
            Log.d("Setting Base Path", path)
            val f = File(path)
            if (f.canWrite()) {
                // hell yeah :)
                dir.setDownloadDirectory(path)
                showPopUpMessage(
                    "Download Directory Set to:\n${dir.defaultDir()} "
                )
            }else{
                showPopUpMessage(
                    "NO WRITE ACCESS on \n$path ,\nReverting Back to Previous"
                )
            }
        }

        // Show dialog whenever you want by
        chooser.show()
    }

    private fun showPopUpMessage(string: String, long: Boolean = false) {
        android.widget.Toast.makeText(
            applicationContext,
            string,
            if(long) android.widget.Toast.LENGTH_LONG else android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionGranted.value = checkPermissions()
    }

    private fun spotiFlyerRoot(componentContext: ComponentContext): SpotiFlyerRoot =
        SpotiFlyerRoot(
            componentContext,
            dependencies = object : SpotiFlyerRoot.Dependencies{
                override val storeFactory = LoggingStoreFactory(DefaultStoreFactory)
                override val database = this@MainActivity.dir.db
                override val fetchPlatformQueryResult = this@MainActivity.fetcher
                override val directories: Dir = this@MainActivity.dir
                override val downloadProgressReport: MutableSharedFlow<HashMap<String, DownloadStatus>> = trackStatusFlow
                override val actions = object: Actions {

                    override val platformActions = object : PlatformActions {
                        override val imageCacheDir: String = applicationContext.cacheDir.absolutePath + File.separator
                        override val sharedPreferences = applicationContext.getSharedPreferences(SharedPreferencesKey,
                            MODE_PRIVATE
                        )

                        override fun addToLibrary(path: String) {
                            MediaScannerConnection.scanFile (
                                applicationContext,
                                listOf(path).toTypedArray(), null, null
                            )
                        }

                        override fun sendTracksToService(array: ArrayList<TrackDetails>) {
                            for (list in array.chunked(50)) {
                                val serviceIntent = Intent(this@MainActivity, ForegroundService::class.java)
                                serviceIntent.putParcelableArrayListExtra("object", list as ArrayList)
                                ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
                            }
                        }
                    }

                    override fun showPopUpMessage(string: String, long: Boolean) = this@MainActivity.showPopUpMessage(string,long)

                    override fun setDownloadDirectoryAction() = setUpOnPrefClickListener()

                    override fun queryActiveTracks() {
                        val serviceIntent = Intent(this@MainActivity, ForegroundService::class.java).apply {
                            action = "query"
                        }
                        ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
                    }

                    override fun giveDonation() {
                        try {
                            startPayment(this@MainActivity)
                        }catch (e:Exception) {
                            openPlatform("",platformLink = "https://razorpay.com/payment-button/pl_GnKuuDBdBu0ank/view/?utm_source=payment_button&utm_medium=button&utm_campaign=payment_button")
                        }
                    }

                    override fun shareApp() {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Hey, checkout this excellent Music Downloader http://github.com/Shabinder/SpotiFlyer")
                            type = "text/plain"
                        }

                        val shareIntent = Intent.createChooser(sendIntent, null)
                        startActivity(shareIntent)
                    }

                    override fun openPlatform(packageID: String, platformLink: String) {
                        val manager: PackageManager = applicationContext.packageManager
                        try {
                            val intent = manager.getLaunchIntentForPackage(packageID)
                                ?: throw PackageManager.NameNotFoundException()
                            intent.addCategory(Intent.CATEGORY_LAUNCHER)
                            startActivity(intent)
                        } catch (e: PackageManager.NameNotFoundException) {
                            val uri: Uri =
                                Uri.parse(platformLink)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)
                        }
                    }

                    override fun writeMp3Tags(trackDetails: TrackDetails) {/*IMPLEMENTED*/}

                    override val isInternetAvailable get()  = internetAvailability.value ?: true
                }
            }
        )


    @SuppressLint("ObsoleteSdkInt")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == disableDozeCode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val pm =
                    getSystemService(Context.POWER_SERVICE) as PowerManager
                val isIgnoringBatteryOptimizations =
                    pm.isIgnoringBatteryOptimizations(packageName)
                if (isIgnoringBatteryOptimizations) {
                    // Ignoring battery optimization
                    permissionGranted.value = true
                } else {
                    disableDozeMode(disableDozeCode)//Again Ask For Permission!!
                }
            }
        }
    }

    /*
    * Broadcast Handlers
    * */
    private fun initializeBroadcast(){
        val intentFilter = IntentFilter().apply {
            addAction(Status.QUEUED.name)
            addAction(Status.FAILED.name)
            addAction(Status.DOWNLOADING.name)
            addAction(Status.COMPLETED.name)
            addAction("Progress")
            addAction("Converting")
        }
        updateUIReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                //Update Flow with latest details
                if (intent != null) {
                    val trackDetails = intent.getParcelableExtra<TrackDetails?>("track")
                    trackDetails?.let { track ->
                        lifecycleScope.launch {
                            val latestMap = trackStatusFlow.replayCache.getOrElse(0
                            ) { hashMapOf() }.apply {
                                this[track.title] = when (intent.action) {
                                    Status.QUEUED.name -> DownloadStatus.Queued
                                    Status.FAILED.name -> DownloadStatus.Failed
                                    Status.DOWNLOADING.name -> DownloadStatus.Downloading()
                                    "Progress" ->  DownloadStatus.Downloading(intent.getIntExtra("progress", 0))
                                    "Converting" -> DownloadStatus.Converting
                                    Status.COMPLETED.name -> DownloadStatus.Downloaded
                                    else -> DownloadStatus.NotDownloaded
                                }
                            }
                            trackStatusFlow.emit(latestMap)
                            Log.i("Track Update",track.title + track.downloaded.toString())
                        }
                    }
                }
            }
        }
        val queryFilter = IntentFilter().apply { addAction("query_result") }
        queryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                //UI update here
                if (intent != null){
                    @Suppress("UNCHECKED_CAST")
                    val trackList = intent.getSerializableExtra("tracks") as? HashMap<String, DownloadStatus>?
                    trackList?.let { list ->
                        Log.i("Service Response", "${list.size} Tracks Active")
                        lifecycleScope.launch {
                            trackStatusFlow.emit(list)
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


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntentFromExternalActivity(intent)
    }

    private fun handleIntentFromExternalActivity(intent: Intent? = getIntent()) {
        if (intent?.action == Intent.ACTION_SEND) {
            if ("text/plain" == intent.type) {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                    val filterLinkRegex = """http.+\w""".toRegex()
                    val string = it.replace("\n".toRegex(), " ")
                    val link = filterLinkRegex.find(string)?.value.toString()
                    Log.i("Intent",link)
                    lifecycleScope.launch {
                        while(!this@MainActivity::root.isInitialized){
                            delay(100)
                        }
                        if(methods.value.isInternetAvailable)callBacks.searchLink(link)
                    }
                }
            }
        }
    }

    override fun onPaymentError(errorCode: Int, response: String?) {
        try{
            showPopUpMessage("Payment Failed, Response:$response")
        }catch (e: Exception){
            Log.d("Razorpay Payment","Exception in onPaymentSuccess $response")
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        try{
            showPopUpMessage("Payment Successful, ThankYou!")
        }catch (e: Exception){
            showPopUpMessage("Razorpay Payment, Error Occurred.")
            Log.d("Razorpay Payment","Exception in onPaymentSuccess, ${e.message}")
        }
    }

    /*
    * RazorPay Payment
    * */
    private fun startPayment(mainActivity: Activity) {
        val co = Checkout().apply {
            setKeyID("rzp_live_3ZQeoFYOxjmXye")
            setImage(com.shabinder.common.di.R.drawable.ic_spotiflyer_logo)
        }

        try {
            val preFill = JSONObject()

            val options = JSONObject().apply {
                put("name", "SpotiFlyer")
                put("description", "Thanks For the Donation!")
                // You can omit the image option to fetch the image from dashboard
                // put("image","https://github.com/Shabinder/SpotiFlyer/raw/master/app/SpotifyDownload.png")
                put("currency", "INR")
                put("amount", "4900")
                put("prefill", preFill)
            }

            co.open(mainActivity, options)
        } catch (e: Exception) {
            // showPop("Error in payment: "+ e.message)
            e.printStackTrace()
        }
    }
}
