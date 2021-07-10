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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.shabinder.common.di.ConnectionLiveData
import com.shabinder.common.di.Dir
import com.shabinder.common.di.FetchPlatformQueryResult
import com.shabinder.common.di.observeAsState
import com.shabinder.common.di.preference.PreferenceManager
import com.shabinder.common.models.Actions
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.PlatformActions
import com.shabinder.common.models.PlatformActions.Companion.SharedPreferencesKey
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.methods
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.SpotiFlyerRoot.Analytics
import com.shabinder.common.root.callbacks.SpotiFlyerRootCallBacks
import com.shabinder.common.translations.Strings
import com.shabinder.common.uikit.configurations.SpotiFlyerTheme
import com.shabinder.common.uikit.configurations.colorOffWhite
import com.shabinder.common.uikit.screens.SpotiFlyerRootContent
import com.shabinder.spotiflyer.service.ForegroundService
import com.shabinder.spotiflyer.ui.AnalyticsDialog
import com.shabinder.spotiflyer.ui.NetworkDialog
import com.shabinder.spotiflyer.ui.PermissionDialog
import com.shabinder.spotiflyer.utils.checkAppSignature
import com.shabinder.spotiflyer.utils.checkIfLatestVersion
import com.shabinder.spotiflyer.utils.checkPermissions
import com.shabinder.spotiflyer.utils.disableDozeMode
import com.shabinder.spotiflyer.utils.requestStoragePermission
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.matomo.sdk.extra.TrackHelper
import java.io.File


@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {

    private val fetcher: FetchPlatformQueryResult by inject()
    private val dir: Dir by inject()
    private val preferenceManager: PreferenceManager by inject()
    private lateinit var root: SpotiFlyerRoot
    private val callBacks: SpotiFlyerRootCallBacks get() = root.callBacks
    private val trackStatusFlow = MutableSharedFlow<HashMap<String, DownloadStatus>>(1)
    private var permissionGranted = mutableStateOf(true)
    private val internetAvailability by lazy { ConnectionLiveData(applicationContext) }
    private val tracker get() = (application as App).tracker
    private val visibleChild get(): SpotiFlyerRoot.Child = root.routerState.value.activeChild.instance

    // Variable for storing instance of our service class
    var foregroundService: ForegroundService? = null

    // Boolean to check if our activity is bound to service or not
    var isServiceBound: Boolean? = null

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

                        NetworkDialog(isInternetAvailableState())

                        PermissionDialog(
                            permissionGranted.value,
                            { requestStoragePermission() },
                            { disableDozeMode(disableDozeCode) },
                        )

                        var askForAnalyticsPermission by remember { mutableStateOf(false) }
                        AnalyticsDialog(
                            askForAnalyticsPermission,
                            enableAnalytics = {
                                preferenceManager.toggleAnalytics(true)
                                preferenceManager.firstLaunchDone()
                            },
                            dismissDialog = {
                                askForAnalyticsPermission = false
                                preferenceManager.firstLaunchDone()
                            }
                        )

                        LaunchedEffect(view) {
                            permissionGranted.value = checkPermissions()
                            if(preferenceManager.isFirstLaunch) {
                                delay(2500)
                                // Ask For Analytics Permission on first Dialog
                                askForAnalyticsPermission = true
                            }
                        }
                    }
                }
            }
        }
        initialise()
    }

    private fun initialise() {
        val isGithubRelease = checkAppSignature(this)
        /*
        * Only Send an `Update Notification` on Github Release Builds
        * and Track Downloads for all other releases like F-Droid,
        * for `Github Downloads` we will track Downloads using : https://tooomm.github.io/github-release-stats/?username=Shabinder&repository=SpotiFlyer
        * */
        if(isGithubRelease) { checkIfLatestVersion() }
        if(preferenceManager.isAnalyticsEnabled && !isGithubRelease) {
            // Download/App Install Event for F-Droid builds
            TrackHelper.track().download().with(tracker)
        }
        handleIntentFromExternalActivity()

        initForegroundService()
    }

    /*START: Foreground Service Handlers*/
    private fun initForegroundService() {
        // Start and then Bind to the Service
        ContextCompat.startForegroundService(
            this@MainActivity,
            Intent(this, ForegroundService::class.java)
        )
        bindService()
    }

    /**
     * Interface for getting the instance of binder from our service class
     * So client can get instance of our service class and can directly communicate with it.
     */
    private val serviceConnection = object : ServiceConnection {
        val tag = "Service Connection"

        override fun onServiceConnected(className: ComponentName, iBinder: IBinder) {
            Log.d(tag, "connected to service.")
            // We've bound to MyService, cast the IBinder and get MyBinder instance
            val binder = iBinder as ForegroundService.DownloadServiceBinder
            foregroundService = binder.service
            isServiceBound = true
            lifecycleScope.launch {
                foregroundService?.trackStatusFlowMap?.statusFlow?.let {
                    trackStatusFlow.emitAll(it.conflate())
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d(tag, "disconnected from service.")
            isServiceBound = false
        }
    }

    /*Used to bind to our service class*/
    private fun bindService() {
        Intent(this, ForegroundService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    /*Used to unbind from our service class*/
    private fun unbindService() {
        Intent(this, ForegroundService::class.java).also {
            unbindService(serviceConnection)
        }
    }
    /*END: Foreground Service Handlers*/


    @Composable
    private fun isInternetAvailableState(): State<Boolean?> {
        return internetAvailability.observeAsState()
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
                override val fetchQuery = this@MainActivity.fetcher
                override val dir: Dir = this@MainActivity.dir
                override val preferenceManager = this@MainActivity.preferenceManager
                override val downloadProgressFlow: MutableSharedFlow<HashMap<String, DownloadStatus>> = trackStatusFlow
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

                        override fun sendTracksToService(array: List<TrackDetails>) {
                            if (foregroundService == null) initForegroundService()
                            foregroundService?.downloadAllTracks(array)
                        }
                    }

                    override fun showPopUpMessage(string: String, long: Boolean) = this@MainActivity.showPopUpMessage(string,long)

                    override fun setDownloadDirectoryAction() = setUpOnPrefClickListener()

                    override fun queryActiveTracks() = this@MainActivity.queryActiveTracks()

                    override fun giveDonation() {
                        openPlatform("",platformLink = "https://razorpay.com/payment-button/pl_GnKuuDBdBu0ank/view/?utm_source=payment_button&utm_medium=button&utm_campaign=payment_button")
                    }

                    override fun shareApp() {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, Strings.shareMessage())
                            type = "text/plain"
                        }

                        val shareIntent = Intent.createChooser(sendIntent, null)
                        startActivity(shareIntent)
                    }

                    override fun copyToClipboard(text: String) {
                        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("SpotiFlyer Selection", text)
                        clipboard.setPrimaryClip(clip)

                        showPopUpMessage("Text Copied to Clipboard.")
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

                /*
                * Analytics Will Only Be Sent if User Granted us the Permission
                * */
                override val analytics = object: Analytics {
                    override fun appLaunchEvent() {
                        if(preferenceManager.isAnalyticsEnabled){
                            TrackHelper.track()
                                .event("events","App_Launch")
                                .name("App Launch").with(tracker)
                        }
                    }

                    override fun homeScreenVisit() {
                        if(preferenceManager.isAnalyticsEnabled){
                            // HomeScreen Visit Event
                            TrackHelper.track().screen("/main_activity/home_screen")
                                .title("HomeScreen").with(tracker)
                        }
                    }

                    override fun listScreenVisit() {
                        if(preferenceManager.isAnalyticsEnabled){
                            // ListScreen Visit Event
                            TrackHelper.track().screen("/main_activity/list_screen")
                                .title("ListScreen").with(tracker)
                        }
                    }

                    override fun donationDialogVisit() {
                        if (preferenceManager.isAnalyticsEnabled) {
                            // Donation Dialog Open Event
                            TrackHelper.track().screen("/main_activity/donation_dialog")
                                .title("DonationDialog").with(tracker)
                        }
                    }
                }
            }
        )

    private fun queryActiveTracks() {
        lifecycleScope.launch {
            foregroundService?.trackStatusFlowMap?.let { tracksStatus ->
                trackStatusFlow.emit(tracksStatus)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        queryActiveTracks()
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
            .setDialogTitle(Strings.setDownloadDirectory())
            .allowCustomPath(true)
            .setType(StorageChooser.DIRECTORY_CHOOSER)
            .build()

        // get path that the user has chosen
        chooser.setOnSelectListener { path ->
            Log.d("Setting Base Path", path)
            val f = File(path)
            if (f.canWrite()) {
                // hell yeah :)
                preferenceManager.setDownloadDirectory(path)
                showPopUpMessage(Strings.downloadDirectorySetTo("\n${dir.defaultDir()}"))
            }else{
                showPopUpMessage(Strings.noWriteAccess("\n$path "))
            }
        }

        // Show dialog whenever you want by
        chooser.show()
    }

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

    override fun onDestroy() {
        super.onDestroy()
        unbindService()
    }

    companion object {
        const val disableDozeCode = 1223
    }
}
