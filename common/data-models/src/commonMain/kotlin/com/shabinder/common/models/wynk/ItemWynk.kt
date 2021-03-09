package com.shabinder.common.models.wynk

data class ItemWynk(
    val album: String,
    val albumRef: AlbumRefWynk,
    val basicShortUrl: String,
    val branchUrl: String,
    val contentLang: String,
    val contentState: String,
    val count: Int,
    val cues: List<String>,
    val downloadPrice: String,
    val downloadUrl: String,
    val duration: Int, //in Seconds
    val exclusive: Boolean,
    val formats: List<String>,
    val htData: List<HtDataWynk>,
    val id: String,
    val isHt: Boolean,
    val itemContentLang: String,
    val keywords: String,
    val largeImage: String,
    val lyrics_avl: String,
    val ostreamingUrl: String,
    val purchaseUrl: String,
    val rentUrl: String,
    val serverEtag: String,
    val shortUrl: String,
    val smallImage: String, //Cover Image after Replacing 120x120 with 720x720
    val subtitle: String, // String : `ArtistName - TrackName`
    val subtitleId: String, //ARTIST NAME,artist-id , etc //USE SUBTITLE INSTEAD
    val subtitleType: String, // ARTIST etc
    val title: String,
    val type: String, //Song ,etc
    val videoPresent: Boolean
)