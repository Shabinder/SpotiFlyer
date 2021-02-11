package com.shabinder.android

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.SpotiFlyerRootContent
import com.shabinder.common.root.callbacks.SpotiFlyerRootCallBacks
import com.shabinder.common.ui.SpotiFlyerTheme
import com.shabinder.database.Database
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

const val disableDozeCode = 1223

class MainActivity : ComponentActivity() {

    private val database: Database by inject()
    private val fetcher: FetchPlatformQueryResult by inject()
    private val dir: Dir by inject()
    private lateinit var root: SpotiFlyerRoot
    private val callBacks: SpotiFlyerRootCallBacks
        get() = root.callBacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpotiFlyerTheme {
                    root = SpotiFlyerRootContent(rootComponent(::spotiFlyerRoot))
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
