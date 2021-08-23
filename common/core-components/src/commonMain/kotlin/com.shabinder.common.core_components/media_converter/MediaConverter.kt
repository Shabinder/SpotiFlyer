package com.shabinder.common.core_components.media_converter

import com.shabinder.common.core_components.parallel_executor.ParallelExecutor
import com.shabinder.common.core_components.parallel_executor.ParallelProcessor
import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.dispatcherDefault
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import org.koin.core.module.Module

abstract class MediaConverter : ParallelProcessor {

    /*
    * Operations Pool
    * */
    override val parallelExecutor = ParallelExecutor(dispatcherDefault)

    /*
    * By Default AudioQuality Output will be equal to Input's Quality,i.e, Denoted by AudioQuality.UNKNOWN
    * */
    abstract suspend fun convertAudioFile(
        inputFilePath: String,
        outputFilePath: String,
        audioQuality: AudioQuality = AudioQuality.UNKNOWN,
        progressCallbacks: (Long) -> Unit = {},
    ): SuspendableEvent<String, Throwable>
}

internal expect fun mediaConverterModule(): Module