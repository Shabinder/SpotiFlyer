package utils

import jiosaavn.JioSaavnRequests
import jiosaavn.models.SaavnPlaylist
import kotlinx.coroutines.runBlocking

// Test Class- at development Time
fun main() = runBlocking {
    val jioSaavnClient = object : JioSaavnRequests {}
    val resp: SaavnPlaylist? = jioSaavnClient.getPlaylist(
        URL = "https://www.jiosaavn.com/featured/hindi_chartbusters/u-75xwHI4ks_"
    )
    println(resp)
}
