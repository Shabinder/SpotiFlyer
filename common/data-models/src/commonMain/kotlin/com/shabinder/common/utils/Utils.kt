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
    var newFileName = fileName

    // Try to remove all accents first before removing illegal chars
    newFileName = newFileName.replace("[ÁĂẮẶẰẲẴǍÂẤẬẦẨẪÄǞȦǠẠȀÀẢȂĀĄÅǺḀÃ]".toRegex(), "A")
    newFileName = newFileName.replace("[áăắặằẳẵǎâấậầẩẫäǟȧǡạȁàảȃāąåǻḁã]".toRegex(), "a")
    newFileName = newFileName.replace("[ḂḄḆ]".toRegex(), "B")
    newFileName = newFileName.replace("[ḃḅḇ]".toRegex(), "b")
    newFileName = newFileName.replace("[ĆČÇḈĈĊ]".toRegex(), "C")
    newFileName = newFileName.replace("[ćčçḉĉċ]".toRegex(), "c")
    newFileName = newFileName.replace("[ĎḐḒḊḌḎ]".toRegex(), "D")
    newFileName = newFileName.replace("[ďḑḓḋḍḏ]".toRegex(), "d")
    newFileName = newFileName.replace("[ÉĔĚȨḜÊẾỆỀỂỄḘËĖẸȄÈẺȆĒḖḔĘẼḚ]".toRegex(), "E")
    newFileName = newFileName.replace("[éĕěȩḝêếệềểễḙëėẹȅèẻȇēḗḕęẽḛ]".toRegex(), "e")
    newFileName = newFileName.replace("[Ḟ]".toRegex(), "F")
    newFileName = newFileName.replace("[ḟ]".toRegex(), "f")
    newFileName = newFileName.replace("[ǴĞǦĢĜĠḠ]".toRegex(), "G")
    newFileName = newFileName.replace("[ǵğǧģĝġḡ]".toRegex(), "g")
    newFileName = newFileName.replace("[ḪȞḨĤḦḢḤẖ]".toRegex(), "H")
    newFileName = newFileName.replace("[ḫȟḩĥḧḣḥ]".toRegex(), "h")
    newFileName = newFileName.replace("[ÍĬǏÎÏḮİịȉìỉȋīįĩḭ]".toRegex(), "I")
    newFileName = newFileName.replace("[íĭǐîïḯỊȈÌỈȊĪĮĨḬ]".toRegex(), "i")
    newFileName = newFileName.replace("[ǰĵ]".toRegex(), "J")
    newFileName = newFileName.replace("[Ĵ]".toRegex(), "j")
    newFileName = newFileName.replace("[ḰǨĶḲḴ]".toRegex(), "K")
    newFileName = newFileName.replace("[ḱǩķḳḵ]".toRegex(), "k")
    newFileName = newFileName.replace("[ĹĽĻḼḶḸḺ]".toRegex(), "L")
    newFileName = newFileName.replace("[ĺľļḽḷḹḻ]".toRegex(), "l")
    newFileName = newFileName.replace("[ḾṀṂ]".toRegex(), "M")
    newFileName = newFileName.replace("[ḿṁṃ]".toRegex(), "m")
    newFileName = newFileName.replace("[ŃŇŅṊṄṆǸṈÑ]".toRegex(), "N")
    newFileName = newFileName.replace("[ńňņṋṅṇǹṉñ]".toRegex(), "n")
    newFileName = newFileName.replace("[ÓŎǑÔỐỘỒỔỖÖȪȮȰỌŐȌÒỎƠỚỢỜỞỠȎŌṒṐǪǬÕṌṎȬØǾ]".toRegex(), "O")
    newFileName = newFileName.replace("[óŏǒôốộồổỗöȫȯȱọőȍòỏơớợờởỡȏōṓṑǫǭõṍṏȭø]".toRegex(), "o")
    newFileName = newFileName.replace("[ṔṖ]".toRegex(), "P")
    newFileName = newFileName.replace("[ṕṗ]".toRegex(), "p")
    newFileName = newFileName.replace("[ŔŘŖṘṚṜȐȒṞ]".toRegex(), "R")
    newFileName = newFileName.replace("[ŕřŗṙṛṝȑȓṟ]".toRegex(), "r")
    newFileName = newFileName.replace("[ŚṤŠṦŞŜȘṠṢṨ]".toRegex(), "S")
    newFileName = newFileName.replace("[śṥšṧşŝșṡṣṩ]".toRegex(), "s")
    newFileName = newFileName.replace("[ŤŢṰȚẗṫṭṯ]".toRegex(), "T")
    newFileName = newFileName.replace("[ťţṱțṪṬṮ]".toRegex(), "t")
    newFileName = newFileName.replace("[ÚŬǓÛṶÜǗǙǛǕṲỤŰȔÙỦƯỨỰỪỬỮȖŪṺŲŮŨṸṴ]".toRegex(), "U")
    newFileName = newFileName.replace("[úŭǔûṷüǘǚǜǖṳụűȕùủưứựừửữȗūṻųůũṹṵ]".toRegex(), "u")
    newFileName = newFileName.replace("[ṾṼ]".toRegex(), "V")
    newFileName = newFileName.replace("[ṿṽ]".toRegex(), "v")
    newFileName = newFileName.replace("[ẂŴẄẆẈẀẘ]".toRegex(), "W")
    newFileName = newFileName.replace("[ẃŵẅẇẉẁ]".toRegex(), "w")
    newFileName = newFileName.replace("[ẌẊ]".toRegex(), "X")
    newFileName = newFileName.replace("[ẍẋ]".toRegex(), "x")
    newFileName = newFileName.replace("[ÝŶŸẎỴỲỶȲẙỹ]".toRegex(), "Y")
    newFileName = newFileName.replace("[ýŷÿẏỵỳỷȳỸ]".toRegex(), "y")
    newFileName = newFileName.replace("[ŹŽẐŻẒẔ]".toRegex(), "Z")
    newFileName = newFileName.replace("[źžẑżẓẕ]".toRegex(), "z")

    return newFileName.replace("[^\\dA-Za-z0-9-_]".toRegex(), "_")
}
