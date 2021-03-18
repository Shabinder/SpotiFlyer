package com.shabinder.common.di.utils



/**
 * Removing Illegal Chars from File Name
 * **/
fun removeIllegalChars(fileName: String): String {
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