package com.shabinder.common.providers.saavn

import co.touchlab.kermit.Kermit
import com.shabinder.common.core_components.file_manager.FileManager
import com.shabinder.common.core_components.file_manager.finalOutputDir
import com.shabinder.common.core_components.file_manager.getImageCachePath
import com.shabinder.common.models.*
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import com.shabinder.common.models.saavn.SaavnSong
import com.shabinder.common.models.spotify.Source
import com.shabinder.common.providers.saavn.requests.JioSaavnRequests
import com.shabinder.common.utils.removeIllegalChars
import io.ktor.client.*

class SaavnProvider(
    override val httpClient: HttpClient,
    override val logger: Kermit,
    private val fileManager: FileManager
) : JioSaavnRequests {

    suspend fun query(fullLink: String): SuspendableEvent<PlatformQueryResult, Throwable> = SuspendableEvent {
        PlatformQueryResult(
            folderType = "",
            subFolder = "",
            title = "",
            coverUrl = "",
            trackList = listOf(),
            Source.JioSaavn
        ).apply {
            val pageLink = fullLink.substringAfter("saavn.com/").substringBefore("?")
            when {
                pageLink.contains("song/", true) -> {
                    getSong(fullLink).value.let {
                        folderType = "Tracks"
                        subFolder = ""
                        trackList = listOf(it).toTrackDetails(folderType, subFolder)
                        title = it.song
                        coverUrl = it.image.replace("http:", "https:")
                    }
                }
                pageLink.contains("album/", true) -> {
                    getAlbum(fullLink).value.let {
                        folderType = "Albums"
                        subFolder = removeIllegalChars(it.title)
                        trackList = it.songs.toTrackDetails(folderType, subFolder)
                        title = it.title
                        coverUrl = it.image.replace("http:", "https:")
                    }
                }
                pageLink.contains("featured/", true)
                        || pageLink.contains("playlist/", true) -> { // Playlist
                    getPlaylist(fullLink).value.let {
                        folderType = "Playlists"
                        subFolder = removeIllegalChars(it.listname)
                        trackList = it.songs.toTrackDetails(folderType, subFolder)
                        coverUrl = it.image.replace("http:", "https:")
                        title = it.listname
                    }
                }
                else -> {
                    throw SpotiFlyerException.LinkInvalid(fullLink)
                }
            }
        }
    }

    private fun List<SaavnSong>.toTrackDetails(type: String, subFolder: String): List<TrackDetails> = this.map {
        TrackDetails(
            title = it.song,
            artists = it.artistMap.keys.toMutableSet().apply { addAll(it.singers.split(",")) }.toList(),
            durationSec = it.duration.toInt(),
            albumName = it.album,
            albumArtPath = fileManager.getImageCachePath(it.image),
            year = it.year,
            comment = it.copyright_text,
            trackUrl = it.perma_url,
            videoID = it.id,
            downloadLink = it.media_url, // Downloadable Link
            downloaded = it.updateStatusIfPresent(type, subFolder),
            albumArtURL = it.image.replace("http:", "https:"),
            lyrics = it.lyrics ?: it.lyrics_snippet,
            source = Source.JioSaavn,
            audioQuality = if (it.is320Kbps) AudioQuality.KBPS320 else AudioQuality.KBPS160,
            outputFilePath = fileManager.finalOutputDir(it.song, type, subFolder, fileManager.defaultDir() /*".m4a"*/)
        )
    }

    private fun SaavnSong.updateStatusIfPresent(folderType: String, subFolder: String): DownloadStatus {
        return if (fileManager.isPresent(
                fileManager.finalOutputDir(
                    song,
                    folderType,
                    subFolder,
                    fileManager.defaultDir()
                )
            )
        ) { // Download Already Present!!
            DownloadStatus.Downloaded.also {
                downloaded = it
            }
        } else downloaded
    }
}
