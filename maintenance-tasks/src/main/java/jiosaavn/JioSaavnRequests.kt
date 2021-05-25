package jiosaavn

import analytics_html_img.client
import audio_conversion.AudioToMp3
import io.github.shabinder.fuzzywuzzy.diffutils.FuzzySearch
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.http.Parameters
import jiosaavn.models.SaavnAlbum
import jiosaavn.models.SaavnPlaylist
import jiosaavn.models.SaavnSearchResult
import jiosaavn.models.SaavnSong
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import utils.debug

val serializer = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

interface JioSaavnRequests {

    suspend fun findSongDownloadURL(
        trackName: String,
        trackArtists: List<String>,
    ): String? {
        val songs = searchForSong(trackName)
        val bestMatches = sortByBestMatch(songs, trackName, trackArtists)
        val m4aLink = bestMatches.keys.firstOrNull()?.let {
            getSongFromID(it).media_url
        }
        val mp3Link = m4aLink?.let { AudioToMp3.convertToMp3(it) }
        return mp3Link
    }

    suspend fun searchForSong(
        query: String,
        includeLyrics: Boolean = false
    ): List<SaavnSearchResult> {
        /*if (query.startsWith("http") && query.contains("saavn.com")) {
            return listOf(getSong(query))
        }*/

        val searchURL = search_base_url + query
        val results = mutableListOf<SaavnSearchResult>()
        (serializer.parseToJsonElement(client.get(searchURL)) as JsonObject).getJsonObject("songs").getJsonArray("data")?.forEach {
            (it as? JsonObject)?.formatData()?.let { jsonObject ->
                results.add(serializer.decodeFromJsonElement(SaavnSearchResult.serializer(), jsonObject))
            }
        }
        return results
    }

    suspend fun getLyrics(ID: String): String? {
        return (Json.parseToJsonElement(client.get(lyrics_base_url + ID)) as JsonObject)
            .getString("lyrics")
    }

    suspend fun getSong(
        URL: String,
        fetchLyrics: Boolean = false
    ): SaavnSong {
        val id = getSongID(URL)
        val data = ((serializer.parseToJsonElement(client.get(song_details_base_url + id)) as JsonObject)[id] as JsonObject)
            .formatData(fetchLyrics)
        return serializer.decodeFromJsonElement(SaavnSong.serializer(), data)
    }
    suspend fun getSongFromID(
        ID: String,
        fetchLyrics: Boolean = false
    ): SaavnSong {
        val data = ((serializer.parseToJsonElement(client.get(song_details_base_url + ID)) as JsonObject)[ID] as JsonObject)
            .formatData(fetchLyrics)
        return serializer.decodeFromJsonElement(SaavnSong.serializer(), data)
    }

    private suspend fun getSongID(
        URL: String,
    ): String {
        val res = client.get<String>(URL) {
            body = FormDataContent(
                Parameters.build {
                    append("bitrate", "320")
                }
            )
        }
        return try {
            res.split("\"song\":{\"type\":\"")[1].split("\",\"image\":")[0].split("\"id\":\"").last()
        } catch (e: IndexOutOfBoundsException) {
            res.split("\"pid\":\"")[1].split("\",\"").first()
        }
    }

    suspend fun getPlaylist(
        URL: String,
        includeLyrics: Boolean = false
    ): SaavnPlaylist? {
        return try {
            serializer.decodeFromJsonElement(
                SaavnPlaylist.serializer(),
                (serializer.parseToJsonElement(client.get(playlist_details_base_url + getPlaylistID(URL))) as JsonObject)
                    .formatData(includeLyrics)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun getPlaylistID(
        URL: String
    ): String {
        val res = client.get<String>(URL)
        return try {
            res.split("\"type\":\"playlist\",\"id\":\"")[1].split('"')[0]
        } catch (e: IndexOutOfBoundsException) {
            res.split("\"page_id\",\"")[1].split("\",\"")[0]
        }
    }

    suspend fun getAlbum(
        URL: String,
        includeLyrics: Boolean = false
    ): SaavnAlbum? {
        return try {
            serializer.decodeFromJsonElement(
                SaavnAlbum.serializer(),
                (serializer.parseToJsonElement(client.get(album_details_base_url + getAlbumID(URL))) as JsonObject)
                    .formatData(includeLyrics)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun getAlbumID(
        URL: String
    ): String {
        val res = client.get<String>(URL)
        return try {
            res.split("\"album_id\":\"")[1].split('"')[0]
        } catch (e: IndexOutOfBoundsException) {
            res.split("\"page_id\",\"")[1].split("\",\"")[0]
        }
    }

    private suspend fun JsonObject.formatData(
        includeLyrics: Boolean = false
    ): JsonObject {
        return buildJsonObject {
            // Accommodate Incoming Json Object Data
            // And `Format` everything while iterating
            this@formatData.forEach {
                if (it.value is JsonPrimitive && it.value.jsonPrimitive.isString) {
                    put(it.key, it.value.jsonPrimitive.content.format())
                } else {
                    // Format Songs Nested Collection Too
                    if (it.key == "songs" && it.value is JsonArray) {
                        put(
                            it.key,
                            buildJsonArray {
                                getJsonArray("songs")?.forEach { song ->
                                    (song as? JsonObject)?.formatData(includeLyrics)?.let { formattedSong ->
                                        add(formattedSong)
                                    }
                                }
                            }
                        )
                    } else {
                        put(it.key, it.value)
                    }
                }
            }

            try {
                var url = getString("media_preview_url")!!.replace("preview", "aac") // We Will catch NPE
                url = if (getBoolean("320kbps") == true) {
                    url.replace("_96_p.mp4", "_320.mp4")
                } else {
                    url.replace("_96_p.mp4", "_160.mp4")
                }
                // Add Media URL to JSON Object
                put("media_url", url)
            } catch (e: Exception) {
                // e.printStackTrace()
                // DECRYPT Encrypted Media URL
                getString("encrypted_media_url")?.let {
                    put("media_url", decryptURL(it))
                }
                // Check if 320 Kbps is available or not
                if (getBoolean("320kbps") != true && containsKey("media_url")) {
                    put("media_url", getString("media_url")?.replace("_320.mp4", "_160.mp4"))
                }
            }
            // Increase Image Resolution
            put(
                "image",
                getString("image")
                    ?.replace("150x150", "500x500")
                    ?.replace("50x50", "500x500")
            )

            // Fetch Lyrics if Requested
            // Lyrics is HTML Based
            if (includeLyrics) {
                if (getBoolean("has_lyrics") == true) {
                    put("lyrics", getString("id")?.let { getLyrics(it) })
                } else {
                    put("lyrics", "")
                }
            }
        }
    }

    fun sortByBestMatch(
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
                debug("Saavn Removing Common Word:  ", result.toString())
                continue
            }

            // Find artist match
            // Will Be Using Fuzzy Search Because YT Spelling might be mucked up
            // match  = (no of artist names in result) / (no. of artist names on spotify) * 100
            var artistMatchNumber = 0

            // String Containing All Artist Names from JioSaavn Search Result
            val artistListString = mutableSetOf<String>().apply {
                result.more_info?.singers?.split(",")?.let { addAll(it) }
                result.more_info?.primary_artists?.toLowerCase()?.split(",")?.let { addAll(it) }
            }.joinToString(" , ")

            for (artist in trackArtists) {
                if (FuzzySearch.partialRatio(artist.toLowerCase(), artistListString) > 85)
                    artistMatchNumber++
            }

            if (artistMatchNumber == 0) {
                debug("Artist Match Saavn Removing:   $result")
                continue
            }
            val artistMatch: Float = (artistMatchNumber.toFloat() / trackArtists.size) * 100
            val nameMatch: Float = FuzzySearch.partialRatio(resultName, trackName).toFloat() / 100
            val avgMatch = (artistMatch + nameMatch) / 2

            linksWithMatchValue[result.id] = avgMatch
        }
        return linksWithMatchValue.toList().sortedByDescending { it.second }.toMap().also {
            debug("Match Found for $trackName - ${!it.isNullOrEmpty()}")
        }
    }

    companion object {
        // EndPoints
        const val search_base_url = "https://www.jiosaavn.com/api.php?__call=autocomplete.get&_format=json&_marker=0&cc=in&includeMetaTags=1&query="
        const val song_details_base_url = "https://www.jiosaavn.com/api.php?__call=song.getDetails&cc=in&_marker=0%3F_marker%3D0&_format=json&pids="
        const val album_details_base_url = "https://www.jiosaavn.com/api.php?__call=content.getAlbumDetails&_format=json&cc=in&_marker=0%3F_marker%3D0&albumid="
        const val playlist_details_base_url = "https://www.jiosaavn.com/api.php?__call=playlist.getDetails&_format=json&cc=in&_marker=0%3F_marker%3D0&listid="
        const val lyrics_base_url = "https://www.jiosaavn.com/api.php?__call=lyrics.getLyrics&ctx=web6dot0&api_version=4&_format=json&_marker=0%3F_marker%3D0&lyrics_id="
    }
}
