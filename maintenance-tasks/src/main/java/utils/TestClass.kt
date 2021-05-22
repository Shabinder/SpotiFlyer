package utils

import jiosaavn.JioSaavnRequests
import kotlinx.coroutines.runBlocking

// Test Class- at development Time
fun main() = runBlocking {
    val jioSaavnClient = object : JioSaavnRequests {}
    val resp = jioSaavnClient.getSongID(
        queryURL = "https://www.jiosaavn.com/song/nadiyon-paar-let-the-music-play-again-from-roohi/KAM0bj1AAn4"
    )

    debug(jioSaavnClient.getSong(resp.toString()).toString())
}
