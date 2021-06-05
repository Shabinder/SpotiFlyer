package models.github

data class GithubFileContent(
    val decryptedContent: String,
    val sha: String
)