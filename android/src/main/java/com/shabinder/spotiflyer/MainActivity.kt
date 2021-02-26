package com.shabinder.spotiflyer

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.SystemSecurityUpdate
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.rememberRootComponent
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.shabinder.common.database.activityContext
import com.shabinder.common.di.*
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.callbacks.SpotiFlyerRootCallBacks
import com.shabinder.common.uikit.*
import com.shabinder.database.Database
import com.shabinder.spotiflyer.utils.*
import com.tonyodev.fetch2.Status
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.android.ext.android.inject

const val disableDozeCode = 1223

@ExperimentalAnimationApi
class MainActivity : ComponentActivity(), PaymentResultListener {

    private val database: Database by inject()
    private val fetcher: FetchPlatformQueryResult by inject()
    private val dir: Dir by inject()
    private lateinit var root: SpotiFlyerRoot
    private val callBacks: SpotiFlyerRootCallBacks
        get() = root.callBacks
    private val trackStatusFlow = MutableSharedFlow<HashMap<String, DownloadStatus>>(1)
    private var permissionGranted = mutableStateOf(true)
    private lateinit var updateUIReceiver: BroadcastReceiver
    private lateinit var queryReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This app draws behind the system bars, so we want to handle fitting system windows
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SpotiFlyerTheme {
                Surface(contentColor = colorOffWhite) {

                    var statusBarHeight by remember { mutableStateOf(27.dp) }
                    permissionGranted = remember { mutableStateOf(true) }
                    val view = LocalView.current

                    LaunchedEffect(view){
                        permissionGranted.value = checkPermissions()
                        view.setOnApplyWindowInsetsListener { _, insets ->
                            statusBarHeight = insets.systemWindowInsetTop.dp
                            insets
                        }
                    }

                    root = SpotiFlyerRootContent(rememberRootComponent(::spotiFlyerRoot),statusBarHeight)


                    NetworkDialog()
                    PermissionDialog()
                }
            }
        }
        initialise()
    }

    private fun initialise() {
        checkIfLatestVersion()
        dir.createDirectories()
        Checkout.preload(applicationContext)
        handleIntentFromExternalActivity()
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
                override val database = this@MainActivity.database
                override val fetchPlatformQueryResult = this@MainActivity.fetcher
                override val directories: Dir = this@MainActivity.dir
                override val downloadProgressReport: MutableSharedFlow<HashMap<String, DownloadStatus>> = trackStatusFlow
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
                        if(isInternetAvailable)callBacks.searchLink(link)
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

    @Composable
    private fun PermissionDialog(){
        var askForPermission by remember { mutableStateOf(false) }
        LaunchedEffect(Unit){
            delay(2000)
            askForPermission = true
        }
        AnimatedVisibility(
            askForPermission && !permissionGranted.value
        ){
            AlertDialog(
                onDismissRequest = {},
                buttons = {
                    TextButton({
                        requestStoragePermission()
                        disableDozeMode(disableDozeCode)
                    },Modifier.padding(bottom = 16.dp,start = 16.dp,end = 16.dp).fillMaxWidth().background(colorPrimary,shape = SpotiFlyerShapes.medium).padding(horizontal = 8.dp),
                    ){
                        Text("Grant Permissions",color = Color.Black,fontSize = 18.sp,textAlign = TextAlign.Center)
                    }
                },title = {Text("Required Permissions:",style = SpotiFlyerTypography.h5,textAlign = TextAlign.Center)},
                backgroundColor = Color.DarkGray,
                text = {
                    Column{
                        Spacer(modifier = Modifier.padding(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        ) {
                            Icon(Icons.Rounded.SdStorage,"Storage Permission.")
                            Spacer(modifier = Modifier.padding(start = 16.dp))
                            Column {
                                Text(
                                    text = "Storage Permission.",
                                    style = SpotiFlyerTypography.h6.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "To download your favourite songs to this device.",
                                    style = SpotiFlyerTypography.subtitle2,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.SystemSecurityUpdate,"Allow Background Running")
                            Spacer(modifier = Modifier.padding(start = 16.dp))
                            Column {
                                Text(
                                    text = "Background Running.",
                                    style = SpotiFlyerTypography.h6.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "To download all songs in background without any System Interruptions",
                                    style = SpotiFlyerTypography.subtitle2,
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    init {
        activityContext = this
    }
}
