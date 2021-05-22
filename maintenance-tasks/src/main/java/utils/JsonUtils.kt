package utils

/*
* JSON UTILS
* */
fun String.escape(): String {
    val output = StringBuilder()
    for (element in this) {
        val chx = element.toInt()
        assert(chx != 0)
        when {
            element == '\n' -> {
                output.append("\\n")
            }
            element == '\t' -> {
                output.append("\\t")
            }
            element == '\r' -> {
                output.append("\\r")
            }
            element == '\\' -> {
                output.append("\\\\")
            }
            element == '"' -> {
                output.append("\\\"")
            }
            element == '\b' -> {
                output.append("\\b")
            }
            chx >= 0x10000 -> {
                assert(false) { "Java stores as u16, so it should never give us a character that's bigger than 2 bytes. It literally can't." }
            }
            chx > 127 -> {
                output.append(String.format("\\u%04x", chx))
            }
            else -> {
                output.append(element)
            }
        }
    }
    return output.toString()
}

fun String.unescape(): String {
    val builder = StringBuilder()
    var i = 0
    while (i < this.length) {
        val delimiter = this[i]
        i++ // consume letter or backslash
        if (delimiter == '\\' && i < this.length) {

            // consume first after backslash
            val ch = this[i]
            i++
            when (ch) {
                '\\', '/', '"', '\'' -> {
                    builder.append(ch)
                }
                'n' -> builder.append('\n')
                'r' -> builder.append('\r')
                't' -> builder.append(
                    '\t'
                )
                'b' -> builder.append('\b')
                'f' -> builder.append("\\f")
                'u' -> {
                    val hex = StringBuilder()

                    // expect 4 digits
                    if (i + 4 > this.length) {
                        throw RuntimeException("Not enough unicode digits! ")
                    }
                    for (x in this.substring(i, i + 4).toCharArray()) {
                        if (!Character.isLetterOrDigit(x)) {
                            throw RuntimeException("Bad character in unicode escape.")
                        }
                        hex.append(Character.toLowerCase(x))
                    }
                    i += 4 // consume those four digits.
                    val code = hex.toString().toInt(16)
                    builder.append(code.toChar())
                }
                else -> {
                    throw RuntimeException("Illegal escape sequence: \\$ch")
                }
            }
        } else { // it's not a backslash, or it's the last character.
            builder.append(delimiter)
        }
    }
    return builder.toString()
}
