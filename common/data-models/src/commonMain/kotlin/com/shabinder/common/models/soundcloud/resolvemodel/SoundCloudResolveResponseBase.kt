package com.shabinder.common.models.soundcloud.resolvemodel

import com.shabinder.common.models.AudioFormat
import com.shabinder.common.models.soundcloud.Media
import com.shabinder.common.models.soundcloud.PublisherMetadata
import com.shabinder.common.models.soundcloud.User
import com.shabinder.common.models.soundcloud.Visuals
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("kind")
sealed class SoundCloudResolveResponseBase {
    abstract val kind: String

    @SerialName("playlist")
    @Serializable
    data class SoundCloudResolveResponsePlaylist(
        @SerialName("artwork_url")
        val artworkUrl: String = "",
        @SerialName("calculated_artwork_url")
        val calculatedArtworkUrl: String = "", //t500x500, t120x120 //  "https://i1.sndcdn.com/artworks-pjsabv9w0EXW3lBJ-nvjDYg-large.jpg" // https://i1.sndcdn.com/artworks-pjsabv9w0EXW3lBJ-nvjDYg-t500x500.jpg
        @SerialName("created_at")
        val createdAt: String = "",
        val description: String = "",
        @SerialName("display_date")
        val displayDate: String = "",
        val duration: Int = 0,
        override val kind: String = "",
        @SerialName("embeddable_by")
        val embeddableBy: String = "",
        val genre: String = "",
        val id: String = "",
        @SerialName("is_album")
        val isAlbum: Boolean = false,
        @SerialName("label_name")
        val labelName: String = "",
        @SerialName("last_modified")
        val lastModified: String = "",
        val license: String = "",
        @SerialName("likes_count")
        val likesCount: Int = 0,
        @SerialName("managed_by_feeds")
        val managedByFeeds: Boolean = false,
        val permalink: String = "",
        @SerialName("permalink_url")
        val permalinkUrl: String = "",
        val `public`: Boolean = false,
        @SerialName("published_at")
        val publishedAt: String = "",
        @SerialName("purchase_title")
        val purchaseTitle: String = "",
        @SerialName("purchase_url")
        val purchaseUrl: String = "",
        @SerialName("release_date")
        val releaseDate: String = "",
        @SerialName("reposts_count")
        val repostsCount: Int = 0,
        @SerialName("secret_token")
        val secretToken: String = "",
        @SerialName("set_type")
        val setType: String = "",
        val sharing: String = "",
        @SerialName("tag_list")
        val tagList: String = "",
        val title: String = "",  //"Top 50: Hip-hop & Rap"
        @SerialName("track_count")
        val trackCount: Int = 0,
        var tracks: List<SoundCloudResolveResponseTrack> = emptyList(),
        val uri: String = "",
        val user: User = User(),
        @SerialName("user_id")
        val userId: Int = 0
    ) : SoundCloudResolveResponseBase()


    @SerialName("track")
    @Serializable
    data class SoundCloudResolveResponseTrack(
        @SerialName("artwork_url")
        val artworkUrl: String = "",
        val caption: String = "",
        @SerialName("comment_count")
        val commentCount: Int = 0,
        val commentable: Boolean = false,
        @SerialName("created_at")
        val createdAt: String = "",
        val description: String = "",
        @SerialName("display_date")
        val displayDate: String = "",
        @SerialName("download_count")
        val downloadCount: Int = 0,
        val downloadable: Boolean = false,
        val duration: Int = 0,
        @SerialName("embeddable_by")
        val embeddableBy: String = "",
        @SerialName("full_duration")
        val fullDuration: Int = 0,
        val genre: String = "",
        @SerialName("has_downloads_left")
        val hasDownloadsLeft: Boolean = false,
        val id: String = "",
        override val kind: String = "",
        @SerialName("label_name")
        val labelName: String = "",
        @SerialName("last_modified")
        val lastModified: String = "",
        val license: String = "",
        @SerialName("likes_count")
        val likesCount: Int = 0,
        val media: Media = Media(),
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
        @SerialName("purchase_title")
        val purchaseTitle: String = "",
        @SerialName("purchase_url")
        val purchaseUrl: String = "",
        @SerialName("release_date")
        val releaseDate: String = "",
        @SerialName("reposts_count")
        val repostsCount: Int = 0,
        @SerialName("secret_token")
        val secretToken: String = "",
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
        val visuals: Visuals? = null,
        @SerialName("waveform_url")
        val waveformUrl: String = ""
    ) : SoundCloudResolveResponseBase() {
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
}