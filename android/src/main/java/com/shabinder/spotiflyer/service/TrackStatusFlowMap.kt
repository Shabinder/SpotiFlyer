package com.shabinder.spotiflyer.service

import com.shabinder.common.models.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class TrackStatusFlowMap(
    val statusFlow: MutableSharedFlow<HashMap<String,DownloadStatus>>,
    private val scope: CoroutineScope
): HashMap<String,DownloadStatus>() {
    override fun put(key: String, value: DownloadStatus): DownloadStatus? {
        val res = super.put(key, value)
        scope.launch { statusFlow.emit(this@TrackStatusFlowMap) }
        return res
    }
}