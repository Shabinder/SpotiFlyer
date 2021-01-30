package com.willowtreeapps.fuzzywuzzy.diffutils.structs

data class EditOp(

        var type: EditType? = null,
        var spos: Int = 0, // source block pos
        var dpos: Int = 0 // destination block pos
) {

    override fun toString(): String {
        return type!!.name + "(" + spos + "," + dpos + ")"
    }
}

class MatchingBlock {
    var spos: Int = 0
    var dpos: Int = 0
    var length: Int = 0

    override fun toString(): String {
        return "($spos,$dpos,$length)"
    }
}

class OpCode {

    var type: EditType? = null
    var sbeg: Int = 0
    var send: Int = 0
    var dbeg: Int = 0
    var dend: Int = 0

    override fun toString(): String {
        return (type!!.name + "(" + sbeg + "," + send + ","
                + dbeg + "," + dend + ")")
    }
}

enum class EditType {
    DELETE,
    EQUAL,
    INSERT,
    REPLACE,
    KEEP
}