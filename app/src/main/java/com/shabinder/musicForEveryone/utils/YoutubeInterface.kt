package com.shabinder.musicForEveryone.utils

import android.util.Log
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import java.io.IOException

object YoutubeInterface {
    private var youtube: YouTube? = null
    private var query:YouTube.Search.List? = null
    private var apiKey:String = "AIzaSyDuRmMA_2mF56BjlhhNpa0SIbjMgjjFaEI"
    private var apiKey2:String = "AIzaSyCotyqgqmz5qw4-IH0tiezIrIIDHLI2yNs"

    fun youtubeConnector() {
        youtube =
            YouTube.Builder(NetHttpTransport(), JacksonFactory(), HttpRequestInitializer { })
                .setApplicationName("Music For Everyone").build()
        try {
            query = youtube?.search()?.list("id,snippet")
            query?.key = apiKey
            query?.maxResults = 1
            query?.type = "video"
            query?.fields =
                "items(id/videoId,snippet/title,snippet/thumbnails/default/url)"
        } catch (e: IOException) {
            Log.i("YI", "Could not initialize: $e")
        }
    }

    fun search(keywords: String?): List<VideoItem>? {
        Log.i("YI searched for",keywords.toString())
        if (youtube == null){youtubeConnector()}
        query!!.q= keywords
        return try {
            val response = query!!.execute()
            val results =
                response.items
            val items = mutableListOf<VideoItem>()
            for (result in results) {
                val item = VideoItem(
                    id = result.id.videoId,
                    title = result.snippet.title,
//                    description = result.snippet.description,
                    thumbnailUrl = result.snippet.thumbnails.default.url
                )
                items.add(item)
                Log.i("YI links received",item.id)
            }
            items
        } catch (e: IOException) {
            Log.d("YI", "Could not search: $e")
            if(query?.key == apiKey2){return null}
            query?.key = apiKey2
            search(keywords)
        }
    }

    data class VideoItem(
        val id:String,
        val title:String,
//        val description: String,
        val thumbnailUrl:String
    )

}