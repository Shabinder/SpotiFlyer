package com.shabinder.common.main.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.shabinder.common.main.SpotiFlyerMain
import com.shabinder.common.main.SpotiFlyerMain.Dependencies

internal class SpotiFlyerMainImpl(
    componentContext: ComponentContext,
    dependencies: Dependencies
): SpotiFlyerMain,ComponentContext by componentContext, Dependencies by dependencies {
    override val models: Value<SpotiFlyerMain.Model>
        get() = TODO("Not yet implemented")

    override fun onDownloadRecordClicked(link: String) {
        TODO("Not yet implemented")
    }

    override fun onInputLinkChanged(link: String) {
        TODO("Not yet implemented")
    }

}