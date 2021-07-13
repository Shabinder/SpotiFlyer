package common

/*
* Helper Function to Replace Obsolete Content with new Updated Content
* */
fun getUpdatedContent(
    oldContent: String,
    newInsertionText: String,
    tagName: String
): String {
    return getReplaceableRegex(tagName).replace(
        oldContent,
        getReplacementText(tagName, newInsertionText)
    )
}

private fun getReplaceableRegex(tagName: String): Regex {
    return """${Common.START_SECTION(tagName)}(?s)(.*)${Common.END_SECTION(tagName)}""".toRegex()
}

private fun getReplacementText(
    tagName: String,
    newInsertionText: String
): String {
    return """
            ${Common.START_SECTION(tagName)}
            $newInsertionText
            ${Common.END_SECTION(tagName)}
    """.trimIndent()
}
