package models.github

import kotlinx.serialization.Serializable

@Serializable
data class GithubFileContent(
    val decryptedContent: String,
    val sha: String
)
