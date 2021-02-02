package com.shabinder.common.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun SpotiFlyerMainContent(component: SpotiFlyerMain){
    val model by component.models.collectAsState(SpotiFlyerMain.State())
}