package com.shabinder.common.providers.sound_cloud

import co.touchlab.kermit.Kermit
import com.shabinder.common.core_components.file_manager.FileManager
import com.shabinder.common.core_components.file_manager.finalOutputDir
import com.shabinder.common.core_components.file_manager.getImageCachePath
import com.shabinder.common.models.AudioFormat
import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import com.shabinder.common.models.soundcloud.resolvemodel.SoundCloudResolveResponseBase.SoundCloudResolveResponsePlaylist
import com.shabinder.common.models.soundcloud.resolvemodel.SoundCloudResolveResponseBase.SoundCloudResolveResponseTrack
import com.shabinder.common.models.spotify.Source
import com.shabinder.common.providers.sound_cloud.requests.SoundCloudRequests
import com.shabinder.common.providers.sound_cloud.requests.doAuthenticatedRequest
import com.shabinder.common.utils.requireNotNull
import io.github.shabinder.utils.getString
import io.ktor.client.HttpClient
import kotlinx.serialization.json.JsonObject

class SoundCloudProvider(
    private val logger: Kermit,
    private val fileManager: FileManager,
    override val httpClient: HttpClient,
) : SoundCloudRequests {
    suspend fun query(fullURL: String) = SuspendableEvent {
        PlatformQueryResult(
            folderType = "",
            subFolder = "",
            title = "",
            coverUrl = "",
            trackList = listOf(),
            Source.SoundCloud
        ).apply {
            when (val response = fetchResult(fullURL)) {
                is SoundCloudResolveResponseTrack -> {
                    folderType = "Tracks"
                    subFolder = ""
                    trackList = listOf(response).toTrackDetailsList(folderType, subFolder)
                    coverUrl = response.artworkUrl
                    title = response.title
                }
                is SoundCloudResolveResponsePlaylist -> {
                    folderType = "Playlists"
                    subFolder = response.title
                    trackList = response.tracks.toTrackDetailsList(folderType, subFolder)
                    coverUrl = response.artworkUrl.formatArtworkUrl()
                        .ifBlank { response.calculatedArtworkUrl.formatArtworkUrl() }
                    title = response.title
                }
            }
        }
    }

    suspend fun getDownloadURL(trackDetails: TrackDetails) = SuspendableEvent {
        doAuthenticatedRequest<JsonObject>(trackDetails.videoID.requireNotNull()).getString("url")
    }


    private fun List<SoundCloudResolveResponseTrack>.toTrackDetailsList(
        type: String,
        subFolder: String
    ): List<TrackDetails> =
        map {
            val downloadableInfo = it.getDownloadableLink()
            TrackDetails(
                title = it.title,
                //trackNumber = it.track_number,
                genre = listOf(it.genre),
                artists = /*it.artists?.map { artist -> artist?.name.toString() } ?:*/ listOf(it.user.username.ifBlank { it.genre }),
                albumArtists = /*it.album?.artists?.mapNotNull { artist -> artist?.name } ?:*/ emptyList(),
                durationSec = (it.duration / 1000),
                albumArtPath = fileManager.getImageCachePath(it.artworkUrl.formatArtworkUrl()),
                albumName = "", //it.album?.name,
                year = runCatching { it.displayDate.substring(0, 4) }.getOrNull(),
                comment = it.caption,
                trackUrl = it.permalinkUrl,
                downloaded = it.updateStatusIfPresent(type, subFolder),
                source = Source.SoundCloud,
                albumArtURL = it.artworkUrl.formatArtworkUrl(),
                outputFilePath = fileManager.finalOutputDir(
                    it.title,
                    type,
                    subFolder,
                    fileManager.defaultDir()/*,".m4a"*/
                ),
                audioQuality = AudioQuality.KBPS128,
                videoID = downloadableInfo?.first,
                audioFormat = downloadableInfo?.second ?: AudioFormat.MP3
            )
        }

    private fun SoundCloudResolveResponseTrack.updateStatusIfPresent(
        folderType: String,
        subFolder: String
    ): DownloadStatus {
        return if (fileManager.isPresent(
                fileManager.finalOutputDir(
                    title,
                    folderType,
                    subFolder,
                    fileManager.defaultDir()
                )
            )
        ) { // Download Already Present!!
            DownloadStatus.Downloaded
        } else
            DownloadStatus.NotDownloaded
    }

    private fun String.formatArtworkUrl(): String {
        return if (isBlank()) ""
        else substringBeforeLast("-") + "-t500x500." + substringAfterLast(".")
    }
}