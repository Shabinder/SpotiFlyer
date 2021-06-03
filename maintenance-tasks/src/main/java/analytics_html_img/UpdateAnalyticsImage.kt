package analytics_html_img

import io.ktor.client.features.timeout
import io.ktor.client.request.head
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.runBlocking
import utils.RETRY_LIMIT_EXHAUSTED
import utils.debug

internal fun updateAnalyticsImage() {
    val secrets = Secrets.initSecrets()
    // debug("fun main: secrets -> $secrets")

    runBlocking {
        val oldGithubFile = GithubService.getGithubFileContent(
            token = secrets.githubToken,
            ownerName = secrets.ownerName,
            repoName = secrets.repoName,
            branchName = secrets.branchName,
            fileName = "README.md"
        )
        // debug("OLD FILE CONTENT",oldGithubFile)
        val imageURL = getAnalyticsImage().also {
            debug("Updated IMAGE", it)
        }

        val replacementText = """
            ${Common.START_SECTION(secrets.tagName)}
            ![Today's Analytics]($imageURL)
            ${Common.END_SECTION(secrets.tagName)}
        """.trimIndent()
        debug("Updated Text to be Inserted", replacementText)

        val regex = """${Common.START_SECTION(secrets.tagName)}(?s)(.*)${Common.END_SECTION(secrets.tagName)}""".toRegex()
        val updatedContent = regex.replace(
            oldGithubFile.decryptedContent,
            replacementText
        )
        // debug("Updated File Content",updatedContent)

        val updationResponse = GithubService.updateGithubFileContent(
            token = secrets.githubToken,
            ownerName = secrets.ownerName,
            repoName = secrets.repoName,
            branchName = secrets.branchName,
            fileName = secrets.filePath,
            commitMessage = secrets.commitMessage,
            rawContent = updatedContent,
            sha = oldGithubFile.sha
        )

        debug("File Updation Response", updationResponse.toString())
    }
}

internal suspend fun getAnalyticsImage(): String {
    var contentLength: Long
    var analyticsImage: String
    var retryCount = 5

    do {
        /*
        * Get a new Image from Analytics,
        * -  Use Any Random useless query param ,
        *    As HCTI Demo, `caches value for a specific Link`
        * */
        val randomID = (1..100000).random()
        analyticsImage = HCTIService.getImageURLFromURL(
            url = "http://shabinder.ddns.net:3200/matomo/index.php?module=Widgetize&action=iframe&containerId=VisitOverviewWithGraph&disableLink=0&widget=1&moduleToWidgetize=CoreHome&actionToWidgetize=renderWidgetContainer&idSite=1&period=day&date=yesterday&disableLink=1&widget=$randomID",
            delayInMilliSeconds = 5000
        )

        // Sometimes we get incomplete image, hence verify `content-length`
        val req = client.head<HttpResponse>(analyticsImage) {
            timeout {
                socketTimeoutMillis = 100_000
            }
        }
        contentLength = req.headers["Content-Length"]?.toLong() ?: 0
        debug(contentLength.toString())

        if(retryCount-- == 0){
            // FAIL Gracefully
            throw(RETRY_LIMIT_EXHAUSTED())
        }
    }while (contentLength<1_20_000)
    return analyticsImage
}
