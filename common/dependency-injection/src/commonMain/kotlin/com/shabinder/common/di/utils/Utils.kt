/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.common.di.utils

import io.github.shabinder.TargetPlatforms
import io.github.shabinder.activePlatform
import kotlinx.serialization.json.Json
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
val json by lazy {
    Json {
        isLenient = true
        ignoreUnknownKeys = true
    }
}

/**
 * Removing Illegal Chars from File Name
 * **/
fun removeIllegalChars(fileName: String): String {
    if (activePlatform is TargetPlatforms.Js) return fileName
    val illegalCharArray = charArrayOf(
        '/',
        '\n',
        '\r',
        '\t',
        '\u0000',
        '\u000C',
        '`',
        '?',
        '*',
        '\\',
        '<',
        '>',
        '|',
        '\"',
        '.',
        '-',
        '\''
    )

    var name = fileName
    for (c in illegalCharArray) {
        name = fileName.replace(c, '_')
    }
    name = name.replace("\\s".toRegex(), "_")
    name = name.replace("/".toRegex(), "_")
    name = name.replace("\\)".toRegex(), "")
    name = name.replace("\\(".toRegex(), "")
    name = name.replace("\\[".toRegex(), "")
    name = name.replace("]".toRegex(), "")
    name = name.replace("\\.".toRegex(), "")
    name = name.replace("\"".toRegex(), "")
    name = name.replace("\'".toRegex(), "")
    name = name.replace(":".toRegex(), "")
    name = name.replace("\\|".toRegex(), "")
    return name
}
