package com.shabinder.common.utils

import kotlinx.serialization.json.Json
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
val globalJson by lazy {
    Json {
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
}

/**
 * Removing Illegal Chars from File Name
 * **/
fun removeIllegalChars(fileName: String): String {
    return fileName.replace("[^\\dA-Za-z0-9-_]".toRegex(), "_")
}
