package com.shabinder.common.root.integration

import co.touchlab.kermit.Kermit
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.RouterState
import com.arkivanov.decompose.pop
import com.arkivanov.decompose.push
import com.arkivanov.decompose.router
import com.arkivanov.decompose.statekeeper.Parcelable
import com.arkivanov.decompose.statekeeper.Parcelize
import com.arkivanov.decompose.value.Value
import com.shabinder.common.database.getLogger
import com.shabinder.common.di.Dir
import com.shabinder.common.list.SpotiFlyerList
import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.SpotiFlyerRoot.Child
import com.shabinder.common.root.SpotiFlyerRoot.Dependencies
import com.shabinder.common.root.callbacks.SpotiFlyerRootCallBacks
import com.shabinder.common.utils.Consumer
import kotlinx.coroutines.flow.StateFlow

internal class SpotiFlyerRootImpl(
    componentContext: ComponentContext,
    dependencies: Dependencies,
) : SpotiFlyerRoot, ComponentContext by componentContext, Dependencies by dependencies {

    private val router =
        router<Configuration, Child>(
            initialConfiguration = Configuration.Main,
            handleBackButton = true,
            componentFactory = ::createChild
        )

    override val routerState: Value<RouterState<*, Child>> = router.state

    override val callBacks = object : SpotiFlyerRootCallBacks{
        override fun searchLink(link: String) = onMainOutput(SpotiFlyerMain.Output.Search(link))
    }

    private fun createChild(configuration: Configuration, componentContext: ComponentContext): Child =
        when (configuration) {
            is Configuration.Main -> Child.Main(spotiFlyerMain(componentContext))
            is Configuration.List -> Child.List(spotiFlyerList(componentContext, link = configuration.link))
        }

    private fun spotiFlyerMain(componentContext: ComponentContext): SpotiFlyerMain =
        SpotiFlyerMain(
            componentContext = componentContext,
            dependencies = object : SpotiFlyerMain.Dependencies, Dependencies by this {
                override val mainOutput: Consumer<SpotiFlyerMain.Output> = Consumer(::onMainOutput)
                override val dir: Dir = directories
            }
        )

    private fun spotiFlyerList(componentContext: ComponentContext, link: String): SpotiFlyerList =
        SpotiFlyerList(
            componentContext = componentContext,
            dependencies = object : SpotiFlyerList.Dependencies, Dependencies by this {
                override val fetchQuery = fetchPlatformQueryResult
                override val dir: Dir = directories
                override val link: String = link
                override val listOutput : Consumer<SpotiFlyerList.Output> = Consumer(::onListOutput)
                override val downloadProgressFlow = downloadProgressReport
            }
        )

    private fun onMainOutput(output: SpotiFlyerMain.Output) =
        when (output) {
            is SpotiFlyerMain.Output.Search -> router.push(Configuration.List(link = output.link))
        }

    private fun onListOutput(output: SpotiFlyerList.Output): Unit =
        when (output) {
            is SpotiFlyerList.Output.Finished -> router.pop()
        }

    private sealed class Configuration : Parcelable {
        @Parcelize
        object Main : Configuration()

        @Parcelize
        data class List(val link: String) : Configuration()
    }
}
