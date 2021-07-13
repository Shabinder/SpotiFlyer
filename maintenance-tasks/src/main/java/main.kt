import common.GithubService
import common.Secrets
import kotlinx.coroutines.runBlocking
import scripts.updateAnalyticsImage
import scripts.updateDownloadCards
import utils.debug

fun main(args: Array<String>) {
    debug("fun main: args -> ${args.joinToString(";")}")
    val secrets = Secrets.initSecrets()

    runBlocking {

        val githubFileContent = GithubService.getGithubFileContent(
            secrets = secrets,
            fileName = "README.md"
        )

        // Content To be Processed
        var updatedGithubContent: String = githubFileContent.decryptedContent

        // TASK -> Update Analytics Image in Readme
        try {
            updatedGithubContent = updateAnalyticsImage(
                updatedGithubContent,
                secrets
            )
        } catch (e: Exception) {
            debug("Analytics Image Updation Failed", e.message.toString())
        }

        // TASK -> Update Total Downloads Card
        try {
            updatedGithubContent = updateDownloadCards(
                updatedGithubContent,
                secrets.copy(tagName = "DCI")
            )
        } catch (e: Exception) {
            debug("Download Card Updation Failed", e.message.toString())
        }

        // Write New Updated README.md
        GithubService.updateGithubFileContent(
            token = secrets.githubToken,
            ownerName = secrets.ownerName,
            repoName = secrets.repoName,
            branchName = secrets.branchName,
            fileName = secrets.filePath,
            commitMessage = secrets.commitMessage,
            rawContent = updatedGithubContent,
            sha = githubFileContent.sha
        )
    }
}
