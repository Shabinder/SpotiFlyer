package common

import utils.byOptionalProperty
import utils.byProperty

internal data class Parameters(
    val githubToken: String,
    val ownerName: String,
    val repoName: String,
    val branchName: String,
    val filePath: String,
    val imageDescription: String,
    val commitMessage: String,
    val tagName: String
) {
    companion object {
        fun initParameters() = Parameters(
            githubToken = "GH_TOKEN".byProperty,
            ownerName = "OWNER_NAME".byProperty,
            repoName = "REPO_NAME".byProperty,
            branchName = "BRANCH_NAME".byOptionalProperty ?: "main",
            filePath = "FILE_PATH".byOptionalProperty ?: "README.md",
            imageDescription = "IMAGE_DESCRIPTION".byOptionalProperty ?: "IMAGE",
            commitMessage = "COMMIT_MESSAGE".byOptionalProperty ?: "HTML-TO-IMAGE Update",
            tagName = "TAG_NAME".byOptionalProperty ?: "HTI"
            // hctiKey = "HCTI_KEY".analytics_html_img.getByProperty
        )
    }
}
