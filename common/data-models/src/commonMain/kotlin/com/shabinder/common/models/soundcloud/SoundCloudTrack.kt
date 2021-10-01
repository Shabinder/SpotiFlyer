package com.shabinder.common.models.soundcloud


import com.shabinder.common.models.AudioFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SoundCloudTrack(
    @SerialName("artwork_url")
    val artworkUrl: String = "",
    //val caption: Any = Any(),
    @SerialName("comment_count")
    val commentCount: Int = 0,
    val commentable: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = "", //2015-05-21T16:36:39Z
    val description: String = "",
    @SerialName("display_date")
    val displayDate: String = "",
    @SerialName("download_count")
    val downloadCount: Int = 0,
    val downloadable: Boolean = false,
    val duration: Int = 0, //290116
    @SerialName("embeddable_by")
    val embeddableBy: String = "",
    @SerialName("full_duration")
    val fullDuration: Int = 0,
    val genre: String = "",
    @SerialName("has_downloads_left")
    val hasDownloadsLeft: Boolean = false,
    val id: Int = 0,
    val kind: String = "",
    @SerialName("label_name")
    val labelName: String = "",
    @SerialName("last_modified")
    val lastModified: String = "",
    val license: String = "",
    @SerialName("likes_count")
    val likesCount: Int = 0,
    val media: Media = Media(), // Important Data
    @SerialName("monetization_model")
    val monetizationModel: String = "",
    val permalink: String = "",
    @SerialName("permalink_url")
    val permalinkUrl: String = "",
    @SerialName("playback_count")
    val playbackCount: Int = 0,
    val policy: String = "",
    val `public`: Boolean = false,
    @SerialName("publisher_metadata")
    val publisherMetadata: PublisherMetadata = PublisherMetadata(),
    //@SerialName("purchase_title")
    //val purchaseTitle: Any = Any(),
    @SerialName("purchase_url")
    val purchaseUrl: String = "", //"http://itunes.apple.com/us/album/sunrise-ep/id993328519"
    @SerialName("release_date")
    val releaseDate: String = "",
    @SerialName("reposts_count")
    val repostsCount: Int = 0,
    //@SerialName("secret_token")
    //val secretToken: Any = Any(),
    val sharing: String = "",
    val state: String = "",
    @SerialName("station_permalink")
    val stationPermalink: String = "",
    @SerialName("station_urn")
    val stationUrn: String = "",
    val streamable: Boolean = false,
    @SerialName("tag_list")
    val tagList: String = "",
    val title: String = "",
    @SerialName("track_authorization")
    val trackAuthorization: String = "",
    @SerialName("track_format")
    val trackFormat: String = "",
    val uri: String = "",
    val urn: String = "",
    val user: User = User(),
    @SerialName("user_id")
    val userId: Int = 0,
    //val visuals: Any = Any(),
    @SerialName("waveform_url")
    val waveformUrl: String = ""
) {
    fun getDownloadableLink(): Pair<String, AudioFormat>? {
        return (media.transcodings.firstOrNull {
            it.quality == "hq" && (it.format.isProgressive || it.url.contains("progressive"))
        } ?: media.transcodings.firstOrNull {
            it.quality == "sq" && (it.format.isProgressive || it.url.contains("progressive"))
        })?.let {
            it.url to it.audioFormat
        }
    }
}