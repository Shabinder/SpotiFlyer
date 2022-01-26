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
import com.arkivanov.decompose.*
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.router.pop
import com.arkivanov.decompose.router.popWhile
import com.arkivanov.decompose.router.push
import com.arkivanov.decompose.router.router
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.shabinder.common.core_components.analytics.AnalyticsEvent
import com.shabinder.common.core_components.analytics.AnalyticsView
import com.shabinder.common.list.SpotiFlyerList
import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.models.Actions
import com.shabinder.common.models.Consumer
import com.shabinder.common.preference.SpotiFlyerPreference
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.SpotiFlyerRoot.Child
import com.shabinder.common.root.SpotiFlyerRoot.Dependencies
import com.shabinder.common.root.callbacks.SpotiFlyerRootCallBacks
import kotlinx.coroutines.flow.MutableStateFlow

internal class SpotiFlyerRootImpl(
    componentContext: ComponentContext,
    dependencies: Dependencies,
) : SpotiFlyerRoot, ComponentContext by componentContext, Dependencies by dependencies,
    Actions by dependencies.actions {

    init {
        AnalyticsEvent.AppLaunch.track(analyticsManager)
        instanceKeeper.ensureNeverFrozen()
        Actions.instance = dependencies.actions.freeze()
        appInit.init()
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

        override fun showToast(text: String) {
            toastState.value = text
        }
    }

    private fun createChild(
        configuration: Configuration,
        componentContext: ComponentContext,
    ): Child =
        when (configuration) {
            is Configuration.Main -> Child.Main(
                spotiFlyerMain(
                    componentContext,
                    Consumer(::onMainOutput),
                )
            )

            is Configuration.List -> Child.List(
                spotiFlyerList(
                    componentContext,
                    configuration.link,
                    Consumer(::onListOutput),
                )
            )

            is Configuration.Preference -> Child.Preference(
                spotiFlyerPreference(
                    componentContext,
                    Consumer(::onPreferenceOutput),
                )
            )
        }


    private fun spotiFlyerMain(
        componentContext: ComponentContext,
        output: Consumer<SpotiFlyerMain.Output>,
    ): SpotiFlyerMain =
        SpotiFlyerMain(
            componentContext = componentContext,
            dependencies = object : SpotiFlyerMain.Dependencies, Dependencies by this {
                override val mainOutput: Consumer<SpotiFlyerMain.Output> = output
                override val mainAnalytics = object : SpotiFlyerMain.Analytics {
                    override fun donationDialogVisit() {
                        AnalyticsEvent.DonationDialogOpen.track(analyticsManager)
                    }
                }
            }
        )

    private fun spotiFlyerList(
        componentContext: ComponentContext,
        link: String,
        output: Consumer<SpotiFlyerList.Output>
    ): SpotiFlyerList =
        SpotiFlyerList(
            componentContext = componentContext,
            dependencies = object : SpotiFlyerList.Dependencies, Dependencies by this {
                override val link: String = link
                override val listOutput: Consumer<SpotiFlyerList.Output> = output
                override val listAnalytics = object : SpotiFlyerList.Analytics {}
            }
        )

    private fun spotiFlyerPreference(
        componentContext: ComponentContext,
        output: Consumer<SpotiFlyerPreference.Output>
    ): SpotiFlyerPreference =
        SpotiFlyerPreference(
            componentContext = componentContext,
            dependencies = object : SpotiFlyerPreference.Dependencies, Dependencies by this {
                override val prefOutput: Consumer<SpotiFlyerPreference.Output> = output
                override val preferenceAnalytics = object : SpotiFlyerPreference.Analytics {}
            }
        )

    private fun onMainOutput(output: SpotiFlyerMain.Output) =
        when (output) {
            is SpotiFlyerMain.Output.Search -> {
                router.push(Configuration.List(link = output.link))
                AnalyticsView.ListScreen.track(analyticsManager)
            }
        }

    private fun onListOutput(output: SpotiFlyerList.Output): Unit =
        when (output) {
            is SpotiFlyerList.Output.Finished -> {
                if (router.state.value.activeChild.instance is Child.List && router.state.value.backStack.isNotEmpty()) {
                    router.pop()
                }
                AnalyticsView.HomeScreen.track(analyticsManager)
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

    private sealed class Configuration : Parcelable {
        @Parcelize
        object Main : Configuration()

        @Parcelize
        object Preference : Configuration()

        @Parcelize
        data class List(val link: String) : Configuration()
    }
}
