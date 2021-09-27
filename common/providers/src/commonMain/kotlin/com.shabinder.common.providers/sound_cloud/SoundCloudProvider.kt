package com.shabinder.common.providers.sound_cloud

import co.touchlab.kermit.Kermit
import com.shabinder.common.core_components.file_manager.FileManager

class SoundCloudProvider(
    private val logger: Kermit,
    private val fileManager: FileManager,
) {
    suspend fun query(fullURL: String) {

    }
}