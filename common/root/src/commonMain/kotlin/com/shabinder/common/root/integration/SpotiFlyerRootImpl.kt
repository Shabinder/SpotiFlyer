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

package com.shabinder.common.root.integration

import co.touchlab.stately.ensureNeverFrozen
import co.touchlab.stately.freeze
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.RouterState
import com.arkivanov.decompose.pop
import com.arkivanov.decompose.popWhile
import com.arkivanov.decompose.push
import com.arkivanov.decompose.router
import com.arkivanov.decompose.statekeeper.Parcelable
import com.arkivanov.decompose.statekeeper.Parcelize
import com.arkivanov.decompose.value.Value
import com.shabinder.common.di.Dir
import com.shabinder.common.di.currentPlatform
import com.shabinder.common.di.providers.SpotifyProvider
import com.shabinder.common.list.SpotiFlyerList
import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.models.Actions
import com.shabinder.common.models.AllPlatforms
import com.shabinder.common.models.Consumer
import com.shabinder.common.models.methods
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.SpotiFlyerRoot.Child
import com.shabinder.common.root.SpotiFlyerRoot.Dependencies
import com.shabinder.common.root.callbacks.SpotiFlyerRootCallBacks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class SpotiFlyerRootImpl(
    componentContext: ComponentContext,
    private val main: (ComponentContext, output:Consumer<SpotiFlyerMain.Output>)->SpotiFlyerMain,
    private val list: (ComponentContext, link:String, output:Consumer<SpotiFlyerList.Output>)->SpotiFlyerList,
    private val actions: Actions
) : SpotiFlyerRoot, ComponentContext by componentContext {

    constructor(
        componentContext: ComponentContext,
        dependencies: Dependencies,
    ):this(
        componentContext = componentContext,
        main = { childContext,output ->
            spotiFlyerMain(
                childContext,
                output,
                dependencies
            )
        },
        list = { childContext, link, output ->
            spotiFlyerList(
                childContext,
                link,
                output,
                dependencies
            )
        },
        actions = dependencies.actions.freeze()
    ) {
        instanceKeeper.ensureNeverFrozen()
        methods.value = dependencies.actions.freeze()
        /*Authenticate Spotify Client*/
        authenticateSpotify(
            dependencies.fetchPlatformQueryResult.spotifyProvider,
            currentPlatform is AllPlatforms.Js
        )
    }

    private val router =
        router<Configuration, Child>(
            initialConfiguration = Configuration.Main,
            handleBackButton = true,
            childFactory = ::createChild
        )

    override val routerState: Value<RouterState<*, Child>> = router.state

    override val toastState = MutableStateFlow("")

    override val callBacks = object : SpotiFlyerRootCallBacks {
        override fun searchLink(link: String) = onMainOutput(SpotiFlyerMain.Output.Search(link))
        override fun popBackToHomeScreen() {
            router.popWhile {
                it !is Configuration.Main
            }
        }
        override fun showToast(text:String) { toastState.value = text }
        override fun setDownloadDirectory() { actions.setDownloadDirectoryAction() }
    }

    private fun createChild(configuration: Configuration, componentContext: ComponentContext): Child =
        when (configuration) {
            is Configuration.Main -> Child.Main(main(componentContext, Consumer(::onMainOutput)))
            is Configuration.List -> Child.List(list(componentContext, configuration.link, Consumer(::onListOutput)))
        }

    private fun onMainOutput(output: SpotiFlyerMain.Output) =
        when (output) {
            is SpotiFlyerMain.Output.Search -> router.push(Configuration.List(link = output.link))
        }

    private fun onListOutput(output: SpotiFlyerList.Output): Unit =
        when (output) {
            is SpotiFlyerList.Output.Finished -> router.pop()
        }

    private fun authenticateSpotify(spotifyProvider: SpotifyProvider, override:Boolean){
        GlobalScope.launch(Dispatchers.Default) {
            /*Authenticate Spotify Client*/
            spotifyProvider.authenticateSpotifyClient(override)
        }
    }

    private sealed class Configuration : Parcelable {
        @Parcelize
        object Main : Configuration()

        @Parcelize
        data class List(val link: String) : Configuration()
    }
}

private fun spotiFlyerMain(componentContext: ComponentContext, output: Consumer<SpotiFlyerMain.Output> ,dependencies: Dependencies): SpotiFlyerMain =
    SpotiFlyerMain(
        componentContext = componentContext,
        dependencies = object : SpotiFlyerMain.Dependencies, Dependencies by dependencies {
            override val mainOutput: Consumer<SpotiFlyerMain.Output> = output
            override val dir: Dir = directories
        }
    )

private fun spotiFlyerList(componentContext: ComponentContext, link: String, output: Consumer<SpotiFlyerList.Output>, dependencies: Dependencies): SpotiFlyerList =
    SpotiFlyerList(
        componentContext = componentContext,
        dependencies = object : SpotiFlyerList.Dependencies, Dependencies by dependencies {
            override val fetchQuery = fetchPlatformQueryResult
            override val dir: Dir = directories
            override val link: String = link
            override val listOutput: Consumer<SpotiFlyerList.Output> = output
            override val downloadProgressFlow = downloadProgressReport
        }
    )
