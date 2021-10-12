/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.common.providers

import co.touchlab.kermit.Kermit
import com.shabinder.common.core_components.file_manager.FileManager
import com.shabinder.common.core_components.preference_manager.PreferenceManager
import com.shabinder.common.database.DownloadRecordDatabaseQueries
import com.shabinder.common.models.AudioFormat
import com.shabinder.common.models.AudioQuality
import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.common.models.SpotiFlyerException
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.dispatcherIO
import com.shabinder.common.models.event.coroutines.SuspendableEvent
import com.shabinder.common.models.event.coroutines.flatMapError
import com.shabinder.common.models.event.coroutines.onFailure
import com.shabinder.common.models.event.coroutines.onSuccess
import com.shabinder.common.models.event.coroutines.success
import com.shabinder.common.models.spotify.Source
import com.shabinder.common.providers.gaana.GaanaProvider
import com.shabinder.common.providers.saavn.SaavnProvider
import com.shabinder.common.providers.sound_cloud.SoundCloudProvider
import com.shabinder.common.providers.spotify.SpotifyProvider
import com.shabinder.common.providers.youtube.YoutubeProvider
import com.shabinder.common.providers.youtube_music.YoutubeMusic
import com.shabinder.common.providers.youtube_to_mp3.requests.YoutubeMp3
import com.shabinder.common.utils.appendPadded
import com.shabinder.common.utils.buildString
import com.shabinder.common.utils.requireNotNull
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FetchPlatformQueryResult(
    private val gaanaProvider: GaanaProvider,
    private val spotifyProvider: SpotifyProvider,
    private val youtubeProvider: YoutubeProvider,
    private val saavnProvider: SaavnProvider,
    private val soundCloudProvider: SoundCloudProvider,
    private val youtubeMusic: YoutubeMusic,
    private val youtubeMp3: YoutubeMp3,
    val fileManager: FileManager,
    val preferenceManager: PreferenceManager,
    val logger: Kermit
) {
    private val db: DownloadRecordDatabaseQueries?
        get() = fileManager.db?.downloadRecordDatabaseQueries

    suspend fun query(link: String): SuspendableEvent<PlatformQueryResult, Throwable> {
        val result = when {
            // SPOTIFY
            link.contains("spotify", true) ->
                spotifyProvider.query(link)

            // YOUTUBE
            link.contains("youtube.com", true) || link.contains("youtu.be", true) ->
                youtubeProvider.query(link)

            // JioSaavn
            link.contains("saavn", true) ->
                saavnProvider.query(link)

            // GAANA
            link.contains("gaana", true) ->
                gaanaProvider.query(link)

            // SoundCloud
            link.contains("soundcloud", true) ->
                soundCloudProvider.query(link)

            else -> {
                SuspendableEvent.error(SpotiFlyerException.LinkInvalid(link))
            }
        }
        result.success {
            addToDatabaseAsync(
                link,
                it.copy() // Send a copy in order to not freeze Result itself
            )
        }
        return result
    }

    // 1) Try Finding on JioSaavn (better quality upto 320KBPS)
    // 2) If Not found try finding on YouTube Music
    suspend fun findBestDownloadLink(
        track: TrackDetails,
        preferredQuality: AudioQuality = preferenceManager.audioQuality
    ): SuspendableEvent<Pair<String, AudioQuality>, Throwable> {
        var downloadLink: String? = null
        var audioQuality = AudioQuality.KBPS192
        var audioFormat = AudioFormat.MP4

        val errorTrace = buildString(track) {
            if (track.videoID != null) {
                // We Already have VideoID
                downloadLink = when (track.source) {
                    Source.JioSaavn -> {
                        AudioFormat.MP4
                        saavnProvider.getSongFromID(track.videoID.requireNotNull()).component1()
                            ?.also { audioQuality = it.audioQuality }
                            ?.media_url
                    }
                    Source.YouTube -> {
                        youtubeMp3.getMp3DownloadLink(
                            track.videoID.requireNotNull(),
                            preferredQuality
                        ).let { ytMp3LinkRes ->
                            if (
                                ytMp3LinkRes is SuspendableEvent.Failure
                                ||
                                ytMp3LinkRes.component1().isNullOrBlank()
                            ) {
                                appendPadded(
                                    "Yt1sMp3 Failed for ${track.videoID}:",
                                    ytMp3LinkRes.component2()?.stackTraceToString()
                                        ?: "couldn't fetch link for ${track.videoID} ,trying manual extraction"
                                )

                                appendLine("Trying Local Extraction")
                                SuspendableEvent {
                                    youtubeProvider.fetchVideoM4aLink(track.videoID.requireNotNull())
                                }.onFailure { throwable ->
                                    appendPadded("YT Manual Extraction Failed!", throwable.stackTraceToString())
                                }.onSuccess {
                                    audioQuality = it.second
                                    audioFormat = AudioFormat.MP4
                                }.component1()?.first
                            } else {
                                audioFormat = AudioFormat.MP3
                                ytMp3LinkRes.component1()
                            }
                        }
                    }
                    Source.SoundCloud -> {
                        audioFormat = track.audioFormat
                        soundCloudProvider.getDownloadURL(track).let {
                            if (it is SuspendableEvent.Failure || it.component1().isNullOrEmpty()) {
                                appendPadded(
                                    "SoundCloud Provider Failed for ${track.title}:",
                                    it.component2()?.stackTraceToString()
                                        ?: "couldn't fetch link for ${track.trackUrl}"
                                )
                                null
                            } else
                                it.component1()
                        }
                    }
                    else -> {
                        appendPadded(
                            "Invalid Arguments",
                            "VideoID with ${track.source} source is not defined how to be handled"
                        )
                        /*We should never reach here for now*/
                        null
                    }
                }
            }
            // if videoID wasn't present || fetching using video ID failed
            if (downloadLink.isNullOrBlank()) {

                // Try Fetching Track from Available Sources
                saavnProvider.findBestSongDownloadURL(
                    trackName = track.title,
                    trackArtists = track.artists,
                    preferredQuality = preferredQuality
                ).onSuccess { (URL, quality) ->
                    audioFormat = AudioFormat.MP4
                    downloadLink = URL
                    audioQuality = quality
                }.flatMapError { saavnError ->
                    appendPadded("Fetching From Saavn Failed:", saavnError.stackTraceToString())
                    // Saavn Failed, Lets Try Fetching Now From Youtube Music
                    youtubeMusic.findSongDownloadURLYT(track, preferredQuality, this).onSuccess { (URL, quality, format) ->
                        downloadLink = URL
                        audioQuality = quality
                        audioFormat = format
                    }.onFailure {
                        // Append Error To StackTrace
                        appendPadded(
                            "Fetching From YT Failed:",
                            it.stackTraceToString()
                        )
                    }
                }
            }
        }
        return if (downloadLink.isNullOrBlank()) SuspendableEvent.error(
            SpotiFlyerException.DownloadLinkFetchFailed(errorTrace)
        ) else {
            track.audioFormat = audioFormat
            SuspendableEvent.success(Pair(downloadLink.requireNotNull(), audioQuality))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun addToDatabaseAsync(link: String, result: PlatformQueryResult) {
        GlobalScope.launch(dispatcherIO) {
            db?.add(
                result.folderType,
                result.title,
                link,
                result.coverUrl,
                result.trackList.size.toLong()
            )
        }
    }
}
