package jiosaavn

import analytics_html_img.client
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

val serializer = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

interface JioSaavnRequests {

    suspend fun searchForSong(
        query: String,
        includeLyrics: Boolean = true
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

    companion object {
        // EndPoints
        const val search_base_url = "https://www.jiosaavn.com/api.php?__call=autocomplete.get&_format=json&_marker=0&cc=in&includeMetaTags=1&query="
        const val song_details_base_url = "https://www.jiosaavn.com/api.php?__call=song.getDetails&cc=in&_marker=0%3F_marker%3D0&_format=json&pids="
        const val album_details_base_url = "https://www.jiosaavn.com/api.php?__call=content.getAlbumDetails&_format=json&cc=in&_marker=0%3F_marker%3D0&albumid="
        const val playlist_details_base_url = "https://www.jiosaavn.com/api.php?__call=playlist.getDetails&_format=json&cc=in&_marker=0%3F_marker%3D0&listid="
        const val lyrics_base_url = "https://www.jiosaavn.com/api.php?__call=lyrics.getLyrics&ctx=web6dot0&api_version=4&_format=json&_marker=0%3F_marker%3D0&lyrics_id="
    }
}
