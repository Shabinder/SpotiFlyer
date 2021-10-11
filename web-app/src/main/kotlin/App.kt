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

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.shabinder.common.core_components.file_manager.DownloadProgressFlow
import com.shabinder.common.core_components.preference_manager.PreferenceManager
import com.shabinder.common.di.ApplicationInit
import com.shabinder.common.models.Actions
import com.shabinder.common.models.PlatformActions
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.database.Database
import extras.renderableChild
import react.PropsWithChildren
import react.RBuilder
import react.RComponent
import react.State
import root.RootR

external interface AppProps : PropsWithChildren {
    var dependencies: AppDependencies
}

@Suppress("FunctionName")
fun RBuilder.App(attrs: AppProps.() -> Unit) {
    return child(App::class) {
        this.attrs(attrs)
    }
}

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED", "NON_EXPORTABLE_TYPE")
@OptIn(ExperimentalJsExport::class)
@JsExport
class App(props: AppProps) : RComponent<AppProps, State>(props) {

    private val lifecycle = LifecycleRegistry()
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)
    private val dependencies = props.dependencies

    override fun RBuilder.render() {
        renderableChild(RootR::class, root)
    }

    private val root = SpotiFlyerRoot(ctx,
        object : SpotiFlyerRoot.Dependencies {
            override val storeFactory: StoreFactory = LoggingStoreFactory(DefaultStoreFactory)
            override val fetchQuery = dependencies.fetchPlatformQueryResult
            override val fileManager = dependencies.fileManager
            override val analyticsManager = dependencies.analyticsManager
            override val appInit: ApplicationInit = dependencies.appInit
            override val preferenceManager: PreferenceManager = dependencies.preferenceManager
            override val database: Database? = fileManager.db
            override val downloadProgressFlow = DownloadProgressFlow
            override val actions = object : Actions {
                override val platformActions = object : PlatformActions {}

                override fun showPopUpMessage(string: String, long: Boolean) {
                    /*TODO("Not yet implemented")*/
                }

                override fun copyToClipboard(text: String) {}

                override fun setDownloadDirectoryAction(callBack: (String) -> Unit) {}

                override fun queryActiveTracks() {}

                override fun giveDonation() {}

                override fun shareApp() {}

                override fun openPlatform(packageID: String, platformLink: String) {}

                override fun writeMp3Tags(trackDetails: TrackDetails) {/*IMPLEMENTED*/
                }

                override val isInternetAvailable: Boolean = true
            }
        }
    )

    override fun componentDidMount() {
        lifecycle.resume()
    }

    override fun componentWillUnmount() {
        lifecycle.destroy()
    }
}