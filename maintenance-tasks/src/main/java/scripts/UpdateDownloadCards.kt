package scripts

import common.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import models.matomo.MatomoDownloads
import utils.RETRY_LIMIT_EXHAUSTED
import utils.debug

internal suspend fun updateDownloadCards(
    fileContent: String? = null,
    secrets: Secrets
): String {

    val oldContent = fileContent ?: GithubService.getGithubFileContent(
        secrets = secrets,
        fileName = "README.md"
    ).decryptedContent

    var totalDownloads: Int = GithubService.getGithubRepoReleasesInfo(
        secrets.ownerName,
        secrets.repoName
    ).let { allReleases ->
        var totalCount = 0

        for (release in allReleases) {
            release.assets.forEach {
                // debug("${it.name}: ${release.tag_name}" ,"Downloads: ${it.download_count}")
                totalCount += it.download_count
            }
        }

        debug("Total Download Count:   $totalCount")

        return@let totalCount
    }

    // Add Matomo Downloads
    client.get<MatomoDownloads>("https://matomo.spotiflyer.ml/?module=API&method=Actions.getDownloads&idSite=1&period=year&date=today&format=JSON&token_auth=anonymous").forEach {
        totalDownloads += it.nb_hits
    }

    return getUpdatedContent(
        oldContent,
        """<a href="https://github.com/Shabinder/SpotiFlyer/releases/latest"><img src="${getDownloadCard(totalDownloads)}" height="125" width="280" alt="Total Downloads"></a>""",
        secrets.tagName
    )
}

private suspend fun getDownloadCard(
    count: Int
): String {
    var contentLength: Long
    var downloadCard: String
    var retryCount = 5

    do {
        downloadCard = HCTIService.getImageURLFromHtml(
            html = getDownloadCardHtml(
                count = count,
                date = getTodayDate()
            ),
            css = downloadCardCSS,
            viewPortHeight = "170",
            viewPortWidth = "385"
        )

        // Sometimes we get incomplete image, hence verify `content-length`
        val req = client.head<HttpResponse>(downloadCard) {
            timeout {
                socketTimeoutMillis = 100_000
            }
        }
        contentLength = req.headers["Content-Length"]?.toLong() ?: 0
        // debug(contentLength.toString())

        if (retryCount-- == 0) {
            // FAIL Gracefully
            throw(RETRY_LIMIT_EXHAUSTED())
        }
    } while (contentLength <40_000)
    return downloadCard
}

fun getDownloadCardHtml(
    count: Int,
    date: String, // ex: 06 Jun 2021
): String {
    return """
        <div class="card-container">
          <div id="card" class="dark-bg">
            <img id="profile-photo" src="https://www.lupusresearch.org/wp-content/uploads/2017/09/resource-downloads-icon.png">
            <div class="text-wrapper">
              <p id="title">Total Downloads</p>
              <p id="source">Github & F-Droid</p>
              <div class="contact-wrapper">
                <a id="count" href="#">
                  $count
                </a>
                <a id="date" href="#">
                  Updated on: $date
                </a>
              </div>
            </div>
          </div>
        </div>
    """.trimIndent()
}

val downloadCardCSS =
    """
        @import url('https://fonts.googleapis.com/css2?family=Poppins&display=swap');

        * {
          margin: 0;
          padding: 0;
        }

        html,
        body {
          overflow: hidden;
        }

        .card-container {
          height: 150px;
          width: 360px;
          padding: 8px 12px;
          display: flex;
          transition: 0.3s;
        }

        #card {
          display: flex;
          align-self: center;
          width: fit-content;
          background: linear-gradient(120deg, #f0f0f0 20%, #f9f9f9 30%); 
          border-radius: 22px;
          padding: 20px 40px;
          margin: 0 auto;
          box-shadow: 4px 8px 20px rgba(0, 0, 0, 0.06);
          transition: 0.3s;
        }

        #card:hover {
          box-shadow: none;
          cursor: pointer;
          transform: translateY(2px)
        }

        #card:hover > #profile-photo {
          opacity: 1
        }

        #profile-photo {
          height: 90px;
          width: 90px;
          border-radius: 100px;
          align-self: center;
          box-shadow: 0 6px 30px rgba(199, 199, 199, 0.5);
          opacity: 0.8;
          transition: 0.3s;
        }

        .text-wrapper {
          font-family: 'Poppins', sans-serif;
          line-height: 0;
          align-self: center;
          margin-left: 20px;
        }

        .text-wrapper p {
          margin: 0;
        }

        .contact-wrapper a {
          display: block;
          white-space: nowrap;
          text-decoration: none;
        }

        #title {
          font-size: 20px;
          color: #5f5f5f;
          margin-bottom: 20px;
        }

        #source {
          font-size: 12px;
          color: #9B9B9B;
          margin-bottom: 22px;
        }

        #count {
          padding-top: 8px;
          font-size: 30px;
          color: #615F5F;
          margin-top: 15px;
          transition: 0.3s;
        }
        #date {
          padding-top: 12px;
          font-size: 14px;
          color: #615F5F;
          margin-top: 15px;
          transition: 0.3s;
        }

        #count:hover,
        #date:hover {
          color: #9B9B9B;
        }
    """.trimIndent()
