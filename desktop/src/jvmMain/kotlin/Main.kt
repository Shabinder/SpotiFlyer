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

import androidx.compose.desktop.AppManager
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.rememberRootComponent
import com.arkivanov.mvikotlin.core.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.core.lifecycle.resume
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.shabinder.common.di.Dir
import com.shabinder.common.di.DownloadProgressFlow
import com.shabinder.common.di.FetchPlatformQueryResult
import com.shabinder.common.di.firstLaunchDone
import com.shabinder.common.di.initKoin
import com.shabinder.common.di.isFirstLaunch
import com.shabinder.common.di.isInternetAccessible
import com.shabinder.common.di.setDownloadDirectory
import com.shabinder.common.di.toggleAnalytics
import com.shabinder.common.models.Actions
import com.shabinder.common.models.PlatformActions
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.uikit.SpotiFlyerColors
import com.shabinder.common.uikit.SpotiFlyerRootContent
import com.shabinder.common.uikit.SpotiFlyerShapes
import com.shabinder.common.uikit.SpotiFlyerTypography
import com.shabinder.common.uikit.colorOffWhite
import com.shabinder.database.Database
import kotlinx.coroutines.runBlocking
import org.piwik.java.tracking.PiwikTracker
import utils.trackAsync
import utils.trackScreenAsync
import java.awt.Desktop
import java.net.URI
import javax.swing.JFileChooser
import javax.swing.JFileChooser.APPROVE_OPTION

private val koin = initKoin(enableNetworkLogs = true).koin
private lateinit var showToast: (String)->Unit
private val tracker: PiwikTracker by lazy {
    PiwikTracker("https://kind-grasshopper-73.telebit.io/matomo/matomo.php")
}

fun main() {

    val lifecycle = LifecycleRegistry()
    lifecycle.resume()

    Window("SpotiFlyer",size = IntSize(450,800)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,
            contentColor = colorOffWhite
        ) {
            DesktopMaterialTheme(
                colors = SpotiFlyerColors,
                typography = SpotiFlyerTypography,
                shapes = SpotiFlyerShapes
            ) {
                val root = SpotiFlyerRootContent(rememberRootComponent(factory = ::spotiFlyerRoot))
                showToast =  root.callBacks::showToast
            }
        }
    }
    // Download Tracking for Desktop Apps for Now will be measured using `Github Releases`
    // https://tooomm.github.io/github-release-stats/?username=Shabinder&repository=SpotiFlyer
}

private fun spotiFlyerRoot(componentContext: ComponentContext): SpotiFlyerRoot =
    SpotiFlyerRoot(
        componentContext = componentContext,
        dependencies = object : SpotiFlyerRoot.Dependencies {
            override val storeFactory = DefaultStoreFactory
            override val fetchPlatformQueryResult: FetchPlatformQueryResult = koin.get()
            override val directories: Dir = koin.get()
            override val database: Database? = directories.db
            override val downloadProgressReport = DownloadProgressFlow
            override val actions = object: Actions {
                override val platformActions = object : PlatformActions {}

                override fun showPopUpMessage(string: String, long: Boolean) {
                    if(::showToast.isInitialized){
                        showToast(string)
                    }
                }

                override fun setDownloadDirectoryAction() {
                    val fileChooser = JFileChooser().apply {
                        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    }
                    when (fileChooser.showOpenDialog(AppManager.focusedWindow?.window)) {
                        APPROVE_OPTION -> {
                            val directory = fileChooser.selectedFile
                            if(directory.canWrite()){
                                directories.setDownloadDirectory(directory.absolutePath)
                                showPopUpMessage("Set New Download Directory:\n${directory.absolutePath}")
                            } else {
                                showPopUpMessage("Cant Write to Selected Directory!")
                            }
                        }
                        else -> {
                            showPopUpMessage("No Directory Selected")
                        }
                    }
                }

                override fun queryActiveTracks() {/**/}

                override fun giveDonation() {
                    openLink("https://razorpay.com/payment-button/pl_GnKuuDBdBu0ank/view/?utm_source=payment_button&utm_medium=button&utm_campaign=payment_button")
                }

                override fun shareApp() = openLink("https://github.com/Shabinder/SpotiFlyer")

                override fun openPlatform(packageID: String, platformLink: String) = openLink(platformLink)

                fun openLink(link:String) {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(URI(link))
                    }
                }

                override fun writeMp3Tags(trackDetails: TrackDetails) {/*IMPLEMENTED*/}

                override val isInternetAvailable: Boolean
                    get() =  runBlocking {
                        isInternetAccessible()
                    }
            }
            override val analytics = object: SpotiFlyerRoot.Analytics {
                override fun appLaunchEvent() {
                    if(directories.isFirstLaunch) {
                        // Enable Analytics on First Launch
                        directories.toggleAnalytics(true)
                        directories.firstLaunchDone()
                    }
                    tracker.trackAsync {
                        eventName = "App Launch"
                        eventAction = "App_Launch"
                        eventCategory = "events"
                    }
                }

                override fun homeScreenVisit() {
                    tracker.trackScreenAsync(
                        screenAddress = "/main_activity/home_screen"
                    ) {
                        actionName = "HomeScreen"
                    }
                }

                override fun listScreenVisit() {
                    tracker.trackScreenAsync(
                        screenAddress = "/main_activity/list_screen"
                    ) {
                        actionName = "ListScreen"
                    }
                }

                override fun donationDialogVisit() {
                    tracker.trackScreenAsync(
                        screenAddress = "/main_activity/donation_dialog"
                    ) {
                        actionName = "DonationDialog"
                    }
                }
            }
        }
    )
