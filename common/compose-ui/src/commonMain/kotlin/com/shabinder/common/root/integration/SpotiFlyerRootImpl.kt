package com.shabinder.common.root.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.RouterState
import com.arkivanov.decompose.pop
import com.arkivanov.decompose.push
import com.arkivanov.decompose.router
import com.arkivanov.decompose.statekeeper.Parcelable
import com.arkivanov.decompose.statekeeper.Parcelize
import com.arkivanov.decompose.value.Value
import com.shabinder.common.Dir
import com.shabinder.common.list.SpotiFlyerList
import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.SpotiFlyerRoot.Child
import com.shabinder.common.root.SpotiFlyerRoot.Dependencies
import com.shabinder.common.utils.Consumer

internal class SpotiFlyerRootImpl(
    componentContext: ComponentContext,
    dependencies: Dependencies
) : SpotiFlyerRoot, ComponentContext by componentContext, Dependencies by dependencies {

    private val router =
        router<Configuration, Child>(
            initialConfiguration = Configuration.Main,
            handleBackButton = true,
            componentFactory = ::createChild
        )

    override val routerState: Value<RouterState<*, Child>> = router.state

    private fun createChild(configuration: Configuration, componentContext: ComponentContext): Child =
        when (configuration) {
            is Configuration.Main -> Child.Main(spotiFlyerMain(componentContext))
            is Configuration.Edit -> Child.List(spotiFlyerList(componentContext, link = configuration.link))
        }

    private fun spotiFlyerMain(componentContext: ComponentContext): SpotiFlyerMain =
        SpotiFlyerMain(
            componentContext = componentContext,
            dependencies = object : SpotiFlyerMain.Dependencies, Dependencies by this {
                override fun mainOutput(searched: SpotiFlyerMain.Output): Consumer<SpotiFlyerMain.Output> = Consumer(::onMainOutput)
            }
        )

    private fun spotiFlyerList(componentContext: ComponentContext, link: String): SpotiFlyerList =
        SpotiFlyerList(
            componentContext = componentContext,
            dependencies = object : SpotiFlyerList.Dependencies, Dependencies by this {
                override val fetchQuery = fetchPlatformQueryResult
                override val dir: Dir = directories
                override val link: String = link

                override fun listOutput(finished: SpotiFlyerList.Output.Finished): Consumer<SpotiFlyerList.Output> =
                    Consumer(::onListOutput)
            }
        )

    private fun onMainOutput(output: SpotiFlyerMain.Output): Unit =
        when (output) {
            is SpotiFlyerMain.Output.Search -> router.push(Configuration.Edit(link = output.link))
        }

    private fun onListOutput(output: SpotiFlyerList.Output): Unit =
        when (output) {
            is SpotiFlyerList.Output.Finished -> router.pop()
        }

    private sealed class Configuration : Parcelable {
        @Parcelize
        object Main : Configuration()

        @Parcelize
        data class Edit(val link: String) : Configuration()
    }
}
