package com.shabinder.common.di.providers

import co.touchlab.kermit.Kermit
import com.shabinder.common.di.Dir
import com.shabinder.common.di.audioToMp3.AudioToMp3
import com.shabinder.common.di.finalOutputDir
import com.shabinder.common.di.saavn.JioSaavnRequests
import com.shabinder.common.di.utils.removeIllegalChars
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.saavn.SaavnSong
import com.shabinder.common.models.spotify.Source
import io.ktor.client.HttpClient

class SaavnProvider(
    override val httpClient: HttpClient,
    override val logger: Kermit,
    override val audioToMp3: AudioToMp3,
    private val dir: Dir,
) : JioSaavnRequests {

    suspend fun query(fullLink: String): PlatformQueryResult {
        val result = PlatformQueryResult(
            folderType = "",
            subFolder = "",
            title = "",
            coverUrl = "",
            trackList = listOf(),
            Source.JioSaavn
        )
        with(result) {
            when (fullLink.substringAfter("saavn.com/").substringBefore("/")) {
                "song" -> {
                    getSong(fullLink).let {
                        folderType = "Tracks"
                        subFolder = ""
                        trackList = listOf(it).toTrackDetails(folderType, subFolder)
                        title = it.song
                        coverUrl = it.image.replace("http:", "https:")
                    }
                }
                "album" -> {
                    getAlbum(fullLink)?.let {
                        folderType = "Albums"
                        subFolder = removeIllegalChars(it.title)
                        trackList = it.songs.toTrackDetails(folderType, subFolder)
                        title = it.title
                        coverUrl = it.image.replace("http:", "https:")
                    }
                }
                "featured" -> { // Playlist
                    getPlaylist(fullLink)?.let {
                        folderType = "Playlists"
                        subFolder = removeIllegalChars(it.listname)
                        trackList = it.songs.toTrackDetails(folderType, subFolder)
                        coverUrl = it.image.replace("http:", "https:")
                        title = it.listname
                    }
                }
                else -> {
                    // Handle Error
                }
            }
        }

        return result
    }

    private fun List<SaavnSong>.toTrackDetails(type: String, subFolder: String): List<TrackDetails> = this.map {
        TrackDetails(
            title = it.song,
            artists = it.artistMap.keys.toMutableSet().apply { addAll(it.singers.split(",")) }.toList(),
            durationSec = it.duration.toInt(),
            albumName = it.album,
            albumArtPath = dir.imageCacheDir() + (it.image.substringBeforeLast('/').substringAfterLast('/')) + ".jpeg",
            year = it.year,
            comment = it.copyright_text,
            trackUrl = it.perma_url,
            videoID = it.id,
            downloadLink = it.media_url, // Downloadable Link
            downloaded = it.updateStatusIfPresent(type, subFolder),
            albumArtURL = it.image.replace("http:", "https:"),
            lyrics = it.lyrics ?: it.lyrics_snippet,
            source = Source.JioSaavn,
            outputFilePath = dir.finalOutputDir(it.song, type, subFolder, dir.defaultDir(), /*".m4a"*/)
        )
    }

    private fun SaavnSong.updateStatusIfPresent(folderType: String, subFolder: String): DownloadStatus {
        return if (dir.isPresent(
                dir.finalOutputDir(
                        song,
                        folderType,
                        subFolder,
                        dir.defaultDir()
                    )
            )
        ) { // Download Already Present!!
            DownloadStatus.Downloaded.also {
                downloaded = it
            }
        } else downloaded
    }
}
