package com.shabinder.common.providers.placeholders

import co.touchlab.kermit.Kermit
import com.shabinder.common.core_components.file_manager.FileManager
import com.shabinder.common.core_components.picture.Picture
import com.shabinder.common.database.getLogger
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import com.shabinder.database.Database

val FileManagerPlaceholder = object : FileManager {
    override val logger: Kermit = Kermit(getLogger())
    override val preferenceManager = PreferenceManagerPlaceholder
    override val mediaConverter = MediaConverterPlaceholder

    override val db: Database? = null

    override fun isPresent(path: String): Boolean = false

    override fun fileSeparator(): String  = "/"

    override fun defaultDir(): String  = "/"

    override fun imageCacheDir(): String = "/"

    override fun createDirectory(dirPath: String) {}

    override suspend fun cacheImage(image: Any, path: String) {}

    override suspend fun loadImage(url: String, reqWidth: Int, reqHeight: Int): Picture {
        TODO("Not yet implemented")
    }

    override suspend fun clearCache() {}

    override suspend fun saveFileWithMetadata(
        mp3ByteArray: ByteArray,
        trackDetails: TrackDetails,
        postProcess: (track: TrackDetails) -> Unit
    ): SuspendableEvent<String, Throwable> = SuspendableEvent.success("")

    override fun addToLibrary(path: String) {}
}