package analytics_html_img

import kotlinx.coroutines.runBlocking
import utils.debug

internal fun updateAnalyticsImage() {
    val secrets = initSecrets()
    // debug("fun main: secrets -> $secrets")

    runBlocking {
        val oldGithubFile = GithubService.getGithubFileContent(
            token = Common.GITHUB_TOKEN,
            ownerName = secrets.ownerName,
            repoName = secrets.repoName,
            branchName = secrets.branchName,
            fileName = "README.md"
        )
        debug("OLD FILE CONTENT:\n$oldGithubFile")

        /*
        * Use Any Random useless query param ,
        * As HCTI Demo, `caches value for a specific Link`
        * */
        val randomID = (1..100000).random()
        val imageURL = HCTIService.getImageURLFromURL(
            url = "https://kind-grasshopper-73.telebit.io/matomo/index.php?module=Widgetize&action=iframe&containerId=VisitOverviewWithGraph&disableLink=0&widget=1&moduleToWidgetize=CoreHome&actionToWidgetize=renderWidgetContainer&idSite=1&period=week&date=today&disableLink=1&widget=$randomID",
            delayInMilliSeconds = 5000
        )
        debug("Updated IMAGE:\n$imageURL")

        val replacementText = """
            ${Common.START_SECTION(secrets.tagName)}
            ![Today's Analytics]($imageURL)
            ${Common.END_SECTION(secrets.tagName)}
        """.trimIndent()
        // debug("Updated Text to be Inserted:\n$replacementText")

        val regex = """${Common.START_SECTION(secrets.tagName)}(?s)(.*)${Common.END_SECTION(secrets.tagName)}""".toRegex()
        val updatedContent = regex.replace(
            oldGithubFile.decryptedContent,
            replacementText
        )
        // debug("Updated File Content:\n$updatedContent")

        val updationResponse = GithubService.updateGithubFileContent(
            token = Common.GITHUB_TOKEN,
            ownerName = secrets.ownerName,
            repoName = secrets.repoName,
            branchName = secrets.branchName,
            fileName = secrets.filePath,
            commitMessage = secrets.commitMessage,
            rawContent = updatedContent,
            sha = oldGithubFile.sha
        )

        debug("File Updation Response:\n$updationResponse")
    }
}
