package common

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.put
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.InternalAPI
import io.ktor.util.encodeBase64
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import models.github.GithubFileContent
import models.github.GithubReleasesInfo

internal object GithubService {

    private const val baseURL = Common.GITHUB_API

    suspend fun getGithubRepoReleasesInfo(
        ownerName: String,
        repoName: String,
    ): GithubReleasesInfo {
        return client.get<GithubReleasesInfo>("$baseURL/repos/$ownerName/$repoName/releases")
    }

    suspend fun getGithubFileContent(
        secrets: Secrets,
        fileName: String = "README.md"
    ): GithubFileContent {
        return getGithubFileContent(
            token = secrets.githubToken,
            ownerName = secrets.ownerName,
            repoName = secrets.repoName,
            branchName = secrets.branchName,
            fileName = fileName
        )
    }

    suspend fun getGithubFileContent(
        token: String,
        ownerName: String,
        repoName: String,
        branchName: String,
        fileName: String,
    ): GithubFileContent {
        val resp = client.get<JsonObject>("$baseURL/repos/$ownerName/$repoName/contents/$fileName?ref=$branchName") {
            headers {
                header("Authorization", "token $token")
            }
        }
        // Get Raw Readme File
        val decodedString = client.get<String>("https://raw.githubusercontent.com/$ownerName/$repoName/$branchName/$fileName") {
            headers {
                header("Authorization", "token $token")
            }
        }
        return GithubFileContent(
            decryptedContent = decodedString,
            sha = resp["sha"]?.jsonPrimitive.toString()
                .removeSurrounding("\"")
        )
    }

    @OptIn(InternalAPI::class)
    suspend fun updateGithubFileContent(
        token: String,
        ownerName: String,
        repoName: String,
        branchName: String,
        fileName: String,
        commitMessage: String,
        rawContent: String,
        sha: String
    ): JsonObject {
        return client.put<JsonObject>("$baseURL/repos/$ownerName/$repoName/contents/$fileName") {
            body = buildJsonObject {
                put("branch", branchName)
                put("message", commitMessage)
                put("content", rawContent.encodeBase64())
                put("sha", sha)
                /*put("committer", buildJsonObject {
                    put("name","Shabinder Singh")
                    put("email","dev.shabinder@gmail.com")
                })*/
            }

            headers {
                header("Authorization", "token $token")
                contentType(ContentType.Application.Json)
            }
        }
    }
}
