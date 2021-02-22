package com.shabinder.android

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
import androidx.compose.material.Surface
import androidx.lifecycle.lifecycleScope
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.rootComponent
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.shabinder.android.utils.checkIfLatestVersion
import com.shabinder.android.utils.disableDozeMode
import com.shabinder.android.utils.requestStoragePermission
import com.shabinder.common.di.Dir
import com.shabinder.common.di.FetchPlatformQueryResult
import com.shabinder.common.di.createDirectories
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.SpotiFlyerRootContent
import com.shabinder.common.root.callbacks.SpotiFlyerRootCallBacks
import com.shabinder.common.ui.SpotiFlyerTheme
import com.shabinder.common.ui.colorOffWhite
import com.shabinder.database.Database
import com.tonyodev.fetch2.Status
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.android.ext.android.inject

const val disableDozeCode = 1223

class MainActivity : ComponentActivity() {

    private val database: Database by inject()
    private val fetcher: FetchPlatformQueryResult by inject()
    private val dir: Dir by inject()
    private lateinit var root: SpotiFlyerRoot
    private val callBacks: SpotiFlyerRootCallBacks
        get() = root.callBacks
    //TODO pass updates from Foreground Service
    private val downloadFlow = MutableStateFlow(hashMapOf<String, DownloadStatus>())

    private lateinit var updateUIReceiver: BroadcastReceiver
    private lateinit var queryReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpotiFlyerTheme {
                Surface(contentColor = colorOffWhite) {
                    root = SpotiFlyerRootContent(rootComponent(::spotiFlyerRoot))
                }
            }
        }
        initialise()
    }

    private fun initialise() {
        checkIfLatestVersion()
        requestStoragePermission()
        disableDozeMode(disableDozeCode)
        dir.createDirectories()
    }

    private fun spotiFlyerRoot(componentContext: ComponentContext): SpotiFlyerRoot =
        SpotiFlyerRoot(
            componentContext,
            dependencies = object : SpotiFlyerRoot.Dependencies{
                override val storeFactory = LoggingStoreFactory(DefaultStoreFactory)
                override val database = this@MainActivity.database
                override val fetchPlatformQueryResult = this@MainActivity.fetcher
                override val directories: Dir = this@MainActivity.dir
                override val downloadProgressReport: StateFlow<HashMap<String, DownloadStatus>> = downloadFlow
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
            addAction("Progress")
            addAction("Converting")
            addAction("track_download_completed")
        }
        updateUIReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                //Update Flow with latest details
                if (intent != null) {
                    val trackDetails = intent.getParcelableExtra<TrackDetails?>("track")
                    trackDetails?.let { track ->
                        lifecycleScope.launch {
                            val latestMap = downloadFlow.value.apply {
                                this[track.title] = when (intent.action) {
                                    Status.QUEUED.name -> DownloadStatus.Queued
                                    Status.FAILED.name -> DownloadStatus.Failed
                                    Status.DOWNLOADING.name -> DownloadStatus.Downloading()
                                    "Progress" ->  DownloadStatus.Downloading(intent.getIntExtra("progress", 0))
                                    "Converting" -> DownloadStatus.Converting
                                    "track_download_completed" -> DownloadStatus.Downloaded
                                    else -> DownloadStatus.NotDownloaded
                                }
                            }
                            downloadFlow.emit(latestMap)
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
                            downloadFlow.emit(list)
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
                    callBacks.searchLink(link)
                }
            }
        }
    }

}
