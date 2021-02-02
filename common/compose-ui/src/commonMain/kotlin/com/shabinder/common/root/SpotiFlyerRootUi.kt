package com.shabinder.common.root

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.shabinder.common.list.SpotiFlyerListContent
import com.shabinder.common.main.SpotiFlyerMainContent
import com.shabinder.common.root.SpotiFlyerRoot.Child

@Composable
fun SpotiFlyerRootContent(component: SpotiFlyerRoot) {
    Children(
        routerState = component.routerState,
        //TODO animation = crossfade()
    ) { child, _ ->
        when (child) {
            is Child.Main -> SpotiFlyerMainContent(component = child.component)
            is Child.List -> SpotiFlyerListContent(component = child.component)
        }
    }
}
