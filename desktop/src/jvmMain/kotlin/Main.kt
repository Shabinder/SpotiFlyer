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
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.rememberRootComponent
import com.arkivanov.mvikotlin.core.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.core.lifecycle.resume
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.shabinder.common.di.Dir
import com.shabinder.common.di.DownloadProgressFlow
import com.shabinder.common.di.FetchPlatformQueryResult
import com.shabinder.common.di.initKoin
import com.shabinder.common.di.isInternetAccessible
import com.shabinder.common.models.Actions
import com.shabinder.common.models.AllPlatforms
import com.shabinder.common.models.PlatformActions
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.uikit.SpotiFlyerColors
import com.shabinder.common.uikit.SpotiFlyerRootContent
import com.shabinder.common.uikit.SpotiFlyerShapes
import com.shabinder.common.uikit.SpotiFlyerTypography
import com.shabinder.common.uikit.colorOffWhite
import com.shabinder.common.uikit.showToast
import com.shabinder.database.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private val koin = initKoin(enableNetworkLogs = true).koin

fun main() {

    val lifecycle = LifecycleRegistry()
    lifecycle.resume()

    Window("SpotiFlyer") {
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
                SpotiFlyerRootContent(rememberRootComponent(factory = ::spotiFlyerRoot))
            }
        }
    }
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

                override fun showPopUpMessage(string: String, long: Boolean) = showToast(string)

                override fun setDownloadDirectoryAction() {}

                override fun queryActiveTracks() {}

                override fun giveDonation() {}

                override fun shareApp() {}

                override fun openPlatform(packageID: String, platformLink: String) {}

                override val dispatcherIO = Dispatchers.IO

                override val isInternetAvailable: Boolean
                    get() {
                        var result = false
                        val job = GlobalScope.launch { result = isInternetAccessible() }
                        while (job.isActive) {/*TODO Better Way*/}
                        return result
                    }

                override val currentPlatform = AllPlatforms.Jvm
            }
        }
    )
