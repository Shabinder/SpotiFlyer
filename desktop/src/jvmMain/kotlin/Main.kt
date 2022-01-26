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

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.github.kokorin.jaffree.JaffreeException
import com.github.kokorin.jaffree.ffmpeg.FFmpeg
import com.shabinder.common.di.*
import com.shabinder.common.core_components.analytics.AnalyticsManager
import com.shabinder.common.core_components.file_manager.DownloadProgressFlow
import com.shabinder.common.core_components.file_manager.FileManager
import com.shabinder.common.core_components.preference_manager.PreferenceManager
import com.shabinder.common.core_components.utils.isInternetAccessible
import com.shabinder.common.models.PlatformActions
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.Actions
import com.shabinder.common.providers.FetchPlatformQueryResult
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.translations.Strings
import com.shabinder.common.uikit.SpotiFlyerLogo
import com.shabinder.common.uikit.configurations.SpotiFlyerColors
import com.shabinder.common.uikit.configurations.SpotiFlyerShapes
import com.shabinder.common.uikit.configurations.SpotiFlyerTypography
import com.shabinder.common.uikit.configurations.colorOffWhite
import com.shabinder.common.uikit.screens.SpotiFlyerRootContent
import com.shabinder.database.Database
import kotlinx.coroutines.runBlocking
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.net.URI
import javax.swing.JFileChooser
import javax.swing.JFileChooser.APPROVE_OPTION

private val koin = initKoin(enableNetworkLogs = true).koin
private lateinit var showToast: (String) -> Unit
private lateinit var appWindow: ComposeWindow

@OptIn(ExperimentalDecomposeApi::class)
fun main() {
    val lifecycle = LifecycleRegistry()
    val rootComponent = spotiFlyerRoot(DefaultComponentContext(lifecycle))
    val windowState = WindowState(width = 450.dp, height = 800.dp)
    singleWindowApplication(
        title = "SpotiFlyer",
        state = windowState,
        icon = BitmapPainter(useResource("drawable/spotiflyer.png", ::loadImageBitmap))
    ) {
        appWindow = window
        LifecycleController(lifecycle, windowState)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,
            contentColor = colorOffWhite
        ) {
            MaterialTheme(
                colors = SpotiFlyerColors,
                typography = SpotiFlyerTypography,
                shapes = SpotiFlyerShapes
            ) {
                val root: SpotiFlyerRoot = SpotiFlyerRootContent(rootComponent)
                showToast = root.callBacks::showToast


                // FFmpeg WARNING
                try {
                    FFmpeg.atPath().addArgument("-version").execute()
                } catch (e: Exception) {
                    if (e is JaffreeException) Actions.instance.showPopUpMessage("WARNING!\nFFmpeg not found at path")
                }
            }
        }
    }

    // Download Tracking for Desktop Apps for Now will be measured using `GitHub Releases`
    // https://tooomm.github.io/github-release-stats/?username=Shabinder&repository=SpotiFlyer
}

private fun spotiFlyerRoot(componentContext: ComponentContext): SpotiFlyerRoot =
    SpotiFlyerRoot(
        componentContext = componentContext,
        dependencies = object : SpotiFlyerRoot.Dependencies {
            override val appInit: ApplicationInit = koin.get()
            override val storeFactory = DefaultStoreFactory()
            override val fetchQuery: FetchPlatformQueryResult = koin.get()
            override val fileManager: FileManager = koin.get()
            override val database: Database? = fileManager.db
            override val analyticsManager: AnalyticsManager = koin.get()
            override val preferenceManager: PreferenceManager = koin.get<PreferenceManager>().also {
                it.analyticsManager = analyticsManager
                // Allow Analytics for Desktop
                analyticsManager.giveConsent()
            }
            override val downloadProgressFlow = DownloadProgressFlow
            override val actions: Actions = object : Actions {
                override val platformActions = object : PlatformActions {}

                override fun showPopUpMessage(string: String, long: Boolean) {
                    if (::showToast.isInitialized) {
                        showToast(string)
                    }
                }

                override fun setDownloadDirectoryAction(callBack: (String) -> Unit) {
                    val fileChooser = JFileChooser().apply {
                        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    }
                    when (fileChooser.showOpenDialog(appWindow)) {
                        APPROVE_OPTION -> {
                            val directory = fileChooser.selectedFile
                            if (directory.canWrite()) {
                                preferenceManager.setDownloadDirectory(directory.absolutePath)
                                callBack(directory.absolutePath)
                                showPopUpMessage("${Strings.setDownloadDirectory()} \n${fileManager.defaultDir()}")
                            } else {
                                showPopUpMessage(Strings.noWriteAccess("\n${directory.absolutePath} "))
                            }
                        }
                        else -> {
                            showPopUpMessage("No Directory Selected")
                        }
                    }
                }

                override fun queryActiveTracks() { /**/
                }

                override fun giveDonation() {
                    openLink("https://razorpay.com/payment-button/pl_GnKuuDBdBu0ank/view/?utm_source=payment_button&utm_medium=button&utm_campaign=payment_button")
                }

                override fun shareApp() = openLink("https://github.com/Shabinder/SpotiFlyer")
                override fun copyToClipboard(text: String) {
                    val data = StringSelection(text)
                    val cb: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    cb.setContents(data, data)
                }

                override fun openPlatform(packageID: String, platformLink: String) = openLink(platformLink)

                fun openLink(link: String) {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(URI(link))
                    }
                }

                override fun writeMp3Tags(trackDetails: TrackDetails) {
                    /*IMPLEMENTED*/
                }

                override val isInternetAvailable: Boolean
                    get() = runBlocking {
                        isInternetAccessible()
                    }
            }
        }
    )
