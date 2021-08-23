package com.shabinder.common.providers.saavn.requests

import com.shabinder.common.utils.unescape

expect suspend fun decryptURL(url: String): String

internal fun String.format(): String {
    return this.unescape()
        .replace("&quot;", "'")
        .replace("&amp;", "&")
        .replace("&#039;", "'")
        .replace("&copy;", "©")
}
