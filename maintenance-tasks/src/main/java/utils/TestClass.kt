package utils

import audio_conversion.AudioToMp3
import io.github.shabinder.fuzzywuzzy.diffutils.FuzzySearch
import jiosaavn.models.SaavnSearchResult
import kotlinx.coroutines.runBlocking

// Test Class- at development Time
fun main(): Unit = runBlocking {
    /*val jioSaavnClient = object : JioSaavnRequests {}
    val resp = jioSaavnClient.searchForSong(
        query = "Ye Faasla"
    )
    println(resp.joinToString("\n"))

    val matches = sortByBestMatch(
        tracks = resp,
        trackName = "Ye Faasla",
        trackArtists = listOf("Shaan", "Hardy")
    )
    debug(matches.toString())

    val link = matches.keys.firstOrNull()?.let {
        jioSaavnClient.getSongFromID(it).media_url
    }
    debug(link.toString())*/
    val link = "https://aac.saavncdn.com/787/956c23404206e8f4822827eff5da61a0_320.mp4"
    val audioConverter = object : AudioToMp3 {}
    val mp3Link = audioConverter.convertToMp3(link.toString())
    debug(mp3Link.toString())
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
