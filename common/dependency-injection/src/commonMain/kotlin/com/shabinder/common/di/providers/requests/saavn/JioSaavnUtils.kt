package com.shabinder.common.di.providers.requests.saavn

import com.shabinder.common.di.utils.unescape

expect suspend fun decryptURL(url: String): String

internal fun String.format(): String {
    return this.unescape()
        .replace("&quot;", "'")
        .replace("&amp;", "&")
        .replace("&#039;", "'")
        .replace("&copy;", "©")
}
