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
        updatedGithubContent = updateAnalyticsImage(
            updatedGithubContent,
            secrets
        )

        // TASK -> Update Total Downloads Card
        updatedGithubContent = updateDownloadCards(
            updatedGithubContent,
            secrets.copy(tagName = "DCI")
        )

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
