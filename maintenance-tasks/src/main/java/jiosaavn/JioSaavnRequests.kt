package jiosaavn

import analytics_html_img.client
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.http.Parameters
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

interface JioSaavnRequests {

    fun searchForSong(
        queryURL: String
    ) {
    }

    suspend fun getSong(
        ID: String,
        fetchLyrics: Boolean = false
    ): JsonObject {
        return ((Json.parseToJsonElement(client.get(song_details_base_url + ID)) as JsonObject)[ID] as JsonObject)
            .formatData(fetchLyrics)
    }

    suspend fun getSongID(
        queryURL: String,
        fetchLyrics: Boolean = false
    ): String? {
        val res = client.get<String>(queryURL) {
            body = FormDataContent(
                Parameters.build {
                    append("bitrate", "320")
                }
            )
        }
        return try {
            res.split("\"song\":{\"type\":\"")[1].split("\",\"image\":")[0].split("\"id\":\"").last()
        } catch (e: IndexOutOfBoundsException) {
            res.split("\"pid\":\"").getOrNull(1)?.split("\",\"")?.firstOrNull()
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
