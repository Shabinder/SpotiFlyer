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
import com.arkivanov.decompose.lifecycle.LifecycleRegistry
import com.arkivanov.decompose.lifecycle.destroy
import com.arkivanov.decompose.lifecycle.resume
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.shabinder.common.di.DownloadProgressFlow
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.database.Database
import extras.renderableChild
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.ReactElement
import root.RootR

external interface AppProps : RProps {
    var dependencies: AppDependencies
}

@Suppress("FunctionName")
fun RBuilder.App(attrs: AppProps.() -> Unit): ReactElement {
    return child(App::class){
        this.attrs(attrs)
    }
}

class App(props: AppProps): RComponent<AppProps, RState>(props) {

    private val lifecycle = LifecycleRegistry()
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)
    private val dependencies = props.dependencies

    private val root = SpotiFlyerRoot(ctx,
        object : SpotiFlyerRoot.Dependencies{
            override val storeFactory: StoreFactory = LoggingStoreFactory(DefaultStoreFactory)
            override val fetchPlatformQueryResult = dependencies.fetchPlatformQueryResult
            override val directories = dependencies.directories
            override val database: Database? = directories.db
            override val showPopUpMessage: (String) -> Unit = {}//TODO
            override val downloadProgressReport = DownloadProgressFlow

        }
    )

    override fun componentDidMount() {
        lifecycle.resume()
    }

    override fun componentWillUnmount() {
        lifecycle.destroy()
    }

    override fun RBuilder.render() {
        renderableChild(RootR::class, root)
    }
}