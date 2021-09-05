package com.shabinder.common.providers.placeholders

import com.shabinder.common.core_components.media_converter.MediaConverter
import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.event.coroutines.SuspendableEvent

val MediaConverterPlaceholder = object : MediaConverter() {
    override suspend fun convertAudioFile(
        inputFilePath: String,
        outputFilePath: String,
        audioQuality: AudioQuality,
        progressCallbacks: (Long) -> Unit
    ): SuspendableEvent<String, Throwable> = SuspendableEvent.success("")
}