package jiosaavn

import io.ktor.util.InternalAPI
import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import utils.unescape
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec

internal suspend fun JsonObject.formatData(
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
                put("lyrics", getString("id")?.let { object : JioSaavnRequests {}.getLyrics(it) })
            } else {
                put("lyrics", "")
            }
        }
    }
}

@Suppress("GetInstance")
@OptIn(InternalAPI::class)
suspend fun decryptURL(url: String): String {
    val dks = DESKeySpec("38346591".toByteArray())
    val keyFactory = SecretKeyFactory.getInstance("DES")
    val key: SecretKey = keyFactory.generateSecret(dks)

    val cipher: Cipher = Cipher.getInstance("DES/ECB/PKCS5Padding").apply {
        init(Cipher.DECRYPT_MODE, key, SecureRandom())
    }

    return cipher.doFinal(url.decodeBase64Bytes())
        .decodeToString()
        .replace("_96.mp4", "_320.mp4")
}

internal fun String.format(): String {
    return this.unescape()
        .replace("&quot;", "'")
        .replace("&amp;", "&")
        .replace("&#039;", "'")
        .replace("&copy;", "©")
}

fun JsonObject.getString(key: String): String? = this[key]?.jsonPrimitive?.content
fun JsonObject.getLong(key: String): Long = this[key]?.jsonPrimitive?.content?.toLongOrNull() ?: 0
fun JsonObject.getInteger(key: String): Int = this[key]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
fun JsonObject.getBoolean(key: String): Boolean? = this[key]?.jsonPrimitive?.content?.toBoolean()
fun JsonObject.getFloat(key: String): Float? = this[key]?.jsonPrimitive?.content?.toFloatOrNull()
fun JsonObject.getDouble(key: String): Double? = this[key]?.jsonPrimitive?.content?.toDoubleOrNull()
fun JsonObject?.getJsonObject(key: String): JsonObject? = this?.get(key)?.jsonObject
fun JsonArray?.getJsonObject(index: Int): JsonObject? = this?.get(index)?.jsonObject
fun JsonObject?.getJsonArray(key: String): JsonArray? = this?.get(key)?.jsonArray
