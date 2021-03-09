package com.shabinder.common.models.wynk



// Use Kotlinx JSON Parsing as in YT Music
data class ShortURLWynk(
    val actualTotal: Int,
    val basicShortUrl: String,
    val branchUrl: String,
    val count: Int,
    val downloadUrl: String,
    val duration: Int,
    val exclusive: Boolean,
    val followCount: String,
    val id: String,
    val isCurated: Boolean,
    val isFollowable: Boolean,
    val isHt: Boolean,
    val itemIds: List<String>,
    val itemTypes: List<String>, //Songs , etc
    val items: List<ItemWynk>,
    val lang: String,
    val largeImage: String, //Cover Image Alternate
    val lastUpdated: Long,
    val offset: Int,
    val owner: String,
    val playIcon: Boolean,
    val playlistImage: String, //Cover Image
    val redesignFeaturedImage: String,
    val shortUrl: String,
    val singers: List<SingerWynk>,
    val smallImage: String,
    val title: String,
    val total: Int,
    val type: String
)