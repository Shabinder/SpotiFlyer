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
import com.shabinder.common.di.dispatcherIO
import com.shabinder.common.list.SpotiFlyerList
import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.models.Actions
import com.shabinder.common.models.Consumer
import com.shabinder.common.models.methods
import com.shabinder.common.preference.SpotiFlyerPreference
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.SpotiFlyerRoot.Analytics
import com.shabinder.common.root.SpotiFlyerRoot.Child
import com.shabinder.common.root.SpotiFlyerRoot.Dependencies
import com.shabinder.common.root.callbacks.SpotiFlyerRootCallBacks
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class SpotiFlyerRootImpl(
    componentContext: ComponentContext,
    private val main: (ComponentContext, output: Consumer<SpotiFlyerMain.Output>) -> SpotiFlyerMain,
    private val list: (ComponentContext, link: String, output: Consumer<SpotiFlyerList.Output>) -> SpotiFlyerList,
    private val preference: (ComponentContext, output: Consumer<SpotiFlyerPreference.Output>) -> SpotiFlyerPreference,
    private val actions: Actions,
    private val analytics: Analytics
) : SpotiFlyerRoot, ComponentContext by componentContext {

    constructor(
        componentContext: ComponentContext,
        dependencies: Dependencies,
    ) : this(
        componentContext = componentContext,
        main = { childContext, output ->
            spotiFlyerMain(childContext, output, dependencies)
        },
        list = { childContext, link, output ->
            spotiFlyerList(childContext, link, output, dependencies)
        },
        preference = { childContext, output ->
            spotiFlyerPreference(childContext, output, dependencies)
        },
        actions = dependencies.actions.freeze(),
        analytics = dependencies.analytics
    ) {
        instanceKeeper.ensureNeverFrozen()
        methods.value = dependencies.actions.freeze()

        /*Init App Launch & Authenticate Spotify Client*/
        initAppLaunchAndAuthenticateSpotify(dependencies.fetchQuery::authenticateSpotifyClient)
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
            if (router.state.value.activeChild.instance !is Child.Main && router.state.value.backStack.isNotEmpty()) {
                router.popWhile {
                    it !is Configuration.Main
                }
            }
        }

        override fun openPreferenceScreen() {
            router.push(Configuration.Preference)
        }

        override fun showToast(text: String) { toastState.value = text }
    }

    private fun createChild(configuration: Configuration, componentContext: ComponentContext): Child =
        when (configuration) {
            is Configuration.Main -> Child.Main(main(componentContext, Consumer(::onMainOutput)))
            is Configuration.List -> Child.List(list(componentContext, configuration.link, Consumer(::onListOutput)))
            is Configuration.Preference -> Child.Preference(preference(componentContext, Consumer(::onPreferenceOutput)),)
        }

    private fun onMainOutput(output: SpotiFlyerMain.Output) =
        when (output) {
            is SpotiFlyerMain.Output.Search -> {
                router.push(Configuration.List(link = output.link))
                analytics.listScreenVisit()
            }
        }

    private fun onListOutput(output: SpotiFlyerList.Output): Unit =
        when (output) {
            is SpotiFlyerList.Output.Finished -> {
                if (router.state.value.activeChild.instance is Child.List && router.state.value.backStack.isNotEmpty()) {
                    router.pop()
                }
                analytics.homeScreenVisit()
            }
        }
    private fun onPreferenceOutput(output: SpotiFlyerPreference.Output): Unit =
        when (output) {
            is SpotiFlyerPreference.Output.Finished -> {
                if (router.state.value.activeChild.instance is Child.Preference && router.state.value.backStack.isNotEmpty()) {
                    router.pop()
                }
                Unit
            }
        }

    @OptIn(DelicateCoroutinesApi::class)
    private fun initAppLaunchAndAuthenticateSpotify(authenticator: suspend () -> Unit) {
        GlobalScope.launch(dispatcherIO) {
            analytics.appLaunchEvent()
            /*Authenticate Spotify Client*/
            authenticator()
        }
    }

    private sealed class Configuration : Parcelable {
        @Parcelize
        object Main : Configuration()

        @Parcelize
        object Preference : Configuration()

        @Parcelize
        data class List(val link: String) : Configuration()
    }
}

private fun spotiFlyerMain(componentContext: ComponentContext, output: Consumer<SpotiFlyerMain.Output>, dependencies: Dependencies): SpotiFlyerMain =
    SpotiFlyerMain(
        componentContext = componentContext,
        dependencies = object : SpotiFlyerMain.Dependencies, Dependencies by dependencies {
            override val mainOutput: Consumer<SpotiFlyerMain.Output> = output
            override val mainAnalytics = object : SpotiFlyerMain.Analytics , Analytics by analytics {}
        }
    )

private fun spotiFlyerList(componentContext: ComponentContext, link: String, output: Consumer<SpotiFlyerList.Output>, dependencies: Dependencies): SpotiFlyerList =
    SpotiFlyerList(
        componentContext = componentContext,
        dependencies = object : SpotiFlyerList.Dependencies, Dependencies by dependencies {
            override val link: String = link
            override val listOutput: Consumer<SpotiFlyerList.Output> = output
            override val listAnalytics = object : SpotiFlyerList.Analytics, Analytics by analytics {}
        }
    )

private fun spotiFlyerPreference(componentContext: ComponentContext, output: Consumer<SpotiFlyerPreference.Output>, dependencies: Dependencies): SpotiFlyerPreference =
    SpotiFlyerPreference(
        componentContext = componentContext,
        dependencies = object : SpotiFlyerPreference.Dependencies, Dependencies by dependencies {
            override val prefOutput: Consumer<SpotiFlyerPreference.Output> = output
            override val preferenceAnalytics = object : SpotiFlyerPreference.Analytics, Analytics by analytics {}
        }
    )
