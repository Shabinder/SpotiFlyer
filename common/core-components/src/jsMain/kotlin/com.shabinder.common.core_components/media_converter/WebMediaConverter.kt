package com.shabinder.common.core_components.media_converter

import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.event.Event
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import org.koin.dsl.bind
import org.koin.dsl.module

class WebMediaConverter: MediaConverter() {
    override suspend fun convertAudioFile(
        inputFilePath: String,
        outputFilePath: String,
        audioQuality: AudioQuality,
        progressCallbacks: (Long) -> Unit
    ): SuspendableEvent<String, Throwable> {
        // TODO("Not yet implemented")
        return SuspendableEvent.error(NotImplementedError())
    }
}

internal actual fun mediaConverterModule() = module {
    single { WebMediaConverter() } bind MediaConverter::class
}