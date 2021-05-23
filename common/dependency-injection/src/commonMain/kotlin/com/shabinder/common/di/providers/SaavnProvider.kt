package com.shabinder.common.di.providers

import co.touchlab.kermit.Kermit
import com.shabinder.common.di.Dir
import com.shabinder.common.di.finalOutputDir
import com.shabinder.common.di.saavn.JioSaavnRequests
import com.shabinder.common.di.utils.removeIllegalChars
import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.PlatformQueryResult
import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.saavn.SaavnSearchResult
import com.shabinder.common.models.saavn.SaavnSong
import com.shabinder.common.models.spotify.Source
import io.github.shabinder.fuzzywuzzy.diffutils.FuzzySearch
import io.ktor.client.HttpClient

class SaavnProvider(
    override val httpClient: HttpClient,
    private val logger: Kermit,
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

    private fun sortByBestMatch(
        tracks: List<SaavnSearchResult>,
        trackName: String,
        trackArtists: List<String>,
    ): Map<String, Float> {

        /*
        * "linksWithMatchValue" is map with Saavn VideoID and its rating/match with 100 as Max Value
        **/
        val linksWithMatchValue = mutableMapOf<String, Float>()

        for (result in tracks) {
            var hasCommonWord = false

            val resultName = result.title.toLowerCase().replace("/", " ")
            val trackNameWords = trackName.toLowerCase().split(" ")

            for (nameWord in trackNameWords) {
                if (nameWord.isNotBlank() && FuzzySearch.partialRatio(nameWord, resultName) > 85) hasCommonWord = true
            }

            // Skip this Result if No Word is Common in Name
            if (!hasCommonWord) {
                // log("Saavn Removing", result.toString())
                continue
            }

            // Find artist match
            // Will Be Using Fuzzy Search Because YT Spelling might be mucked up
            // match  = (no of artist names in result) / (no. of artist names on spotify) * 100
            var artistMatchNumber = 0F

            // String Containing All Artist Names from JioSaavn Search Result
            val artistListString = mutableSetOf<String>().apply {
                result.more_info?.singers?.split(",")?.let { addAll(it) }
                result.more_info?.primary_artists?.toLowerCase()?.split(",")?.let { addAll(it) }
            }.joinToString(" , ")

            for (artist in trackArtists) {
                if (FuzzySearch.partialRatio(artist.toLowerCase(), artistListString) > 85)
                    artistMatchNumber++
            }

            if (artistMatchNumber == 0F) {
                // logger.d{ "Saavn Removing:   $result" }
                continue
            }

            val artistMatch: Float = (artistMatchNumber / trackArtists.size.toFloat()) * 100F
            val nameMatch: Float = FuzzySearch.partialRatio(resultName, trackName).toFloat() / 100F

            val avgMatch = (artistMatch + nameMatch) / 2

            linksWithMatchValue[result.id] = avgMatch
        }
        return linksWithMatchValue.toList().sortedByDescending { it.second }.toMap().also {
            logger.d("Saavn Search") { "Match Found for $trackName - ${!it.isNullOrEmpty()}" }
        }
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
