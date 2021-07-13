package models.github

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class Reactions(
    @JsonNames("+1") val upVotes: Int = 0,
    @JsonNames("-1") val downVotes: Int = 0,
    val confused: Int = 0,
    val eyes: Int = 0,
    val heart: Int = 0,
    val hooray: Int = 0,
    val laugh: Int = 0,
    val rocket: Int = 0,
    val total_count: Int = 0,
    val url: String? = null
)
