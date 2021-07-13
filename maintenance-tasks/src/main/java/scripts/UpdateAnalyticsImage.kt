package scripts

import common.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import utils.RETRY_LIMIT_EXHAUSTED
import utils.debug

internal suspend fun updateAnalyticsImage(
    fileContent: String? = null,
    secrets: Secrets
): String {
    // debug("fun main: secrets -> $secrets")

    val oldContent = fileContent ?: GithubService.getGithubFileContent(
        secrets = secrets,
        fileName = "README.md"
    ).decryptedContent

    // debug("OLD FILE CONTENT",oldGithubFile)
    val imageURL = getAnalyticsImage().also {
        debug("Updated IMAGE", it)
    }

    return getUpdatedContent(
        oldContent,
        "![Today's Analytics]($imageURL)",
        secrets.tagName
    )
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
            url = "https://matomo.spotiflyer.ml/index.php?module=Widgetize&action=iframe&containerId=VisitOverviewWithGraph&disableLink=0&widget=1&moduleToWidgetize=CoreHome&actionToWidgetize=renderWidgetContainer&idSite=1&period=day&date=yesterday&disableLink=1&widget=$randomID",
            delayInMilliSeconds = 5000
        )

        // Sometimes we get incomplete image, hence verify `content-length`
        val req = client.head<HttpResponse>(analyticsImage) {
            timeout {
                socketTimeoutMillis = 100_000
            }
        }
        contentLength = req.headers["Content-Length"]?.toLong() ?: 0
        debug("Content Length for Analytics Image", contentLength.toString())

        if (retryCount-- == 0) {
            // FAIL Gracefully
            throw(RETRY_LIMIT_EXHAUSTED())
        }
    } while (contentLength <1_20_000)

    return analyticsImage
}
